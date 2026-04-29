package com.juviai.gateway.filter;

import com.juviai.common.security.GatewayHmacUtil;
import com.juviai.common.security.GatewaySecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Signs every authenticated request forwarded by the gateway with an HMAC-SHA256
 * signature, enabling downstream services to verify that the request genuinely
 * originated from this trusted gateway.
 *
 * <p>This filter runs <em>after</em> authentication (so the principal is available)
 * but before the request is routed to the downstream service.  It adds:
 * <ul>
 *   <li>{@value GatewayHmacUtil#HEADER_SIGNATURE} — Base64 HMAC of the payload</li>
 *   <li>{@value GatewayHmacUtil#HEADER_TIMESTAMP} — epoch seconds at signing time</li>
 *   <li>{@value GatewayHmacUtil#HEADER_USER_ID}   — authenticated user UUID</li>
 *   <li>{@value GatewayHmacUtil#HEADER_ROLES}     — comma-separated ROLE_ names</li>
 *   <li>{@value GatewayHmacUtil#HEADER_USERNAME}  — user email / login name</li>
 *   <li>{@value GatewayHmacUtil#HEADER_TENANT_ID} — resolved tenant ID</li>
 * </ul>
 *
 * <p>For anonymous (unauthenticated) requests, all identity headers are set to empty
 * strings and the signature still covers the path+timestamp so services can verify
 * the request was intentionally allowed through as public traffic.
 *
 * <p>Ordering: runs <em>after</em> {@link ClaimsPropagationFilter} (HIGHEST+15) to
 * read the already-extracted claims, or independently at HIGHEST+20 if needed.
 * Assigned HIGHEST+25 so it is always the last filter to mutate headers before routing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 25)
public class GatewaySignatureFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewaySignatureFilter.class);

    private final GatewaySecurityProperties props;

    public GatewaySignatureFilter(GatewaySecurityProperties props) {
        this.props = props;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(auth -> extractIdentity(auth, exchange))
                .switchIfEmpty(Mono.fromSupplier(() -> Identity.anonymous()))
                .flatMap(identity -> {
                    long epochSec = Instant.now().getEpochSecond();
                    String path   = exchange.getRequest().getURI().getPath();
                    String sig    = GatewayHmacUtil.sign(
                            props.getSharedSecret(),
                            identity.userId,
                            identity.roles,
                            path,
                            epochSec);

                    ServerHttpRequest signed = exchange.getRequest().mutate()
                            .header(GatewayHmacUtil.HEADER_SIGNATURE, sig)
                            .header(GatewayHmacUtil.HEADER_TIMESTAMP,  String.valueOf(epochSec))
                            .header(GatewayHmacUtil.HEADER_USER_ID,    identity.userId)
                            .header(GatewayHmacUtil.HEADER_ROLES,      identity.roles)
                            .header(GatewayHmacUtil.HEADER_USERNAME,   identity.username)
                            .header(GatewayHmacUtil.HEADER_TENANT_ID,  identity.tenantId)
                            .build();

                    if (log.isDebugEnabled()) {
                        log.debug("Signed gateway request: method={} path={} userId={} roles={}",
                                exchange.getRequest().getMethod(), path, identity.userId, identity.roles);
                    }

                    return chain.filter(exchange.mutate().request(signed).build());
                });
    }

    // ── Identity extraction ───────────────────────────────────────────────────

    private Identity extractIdentity(Authentication auth, ServerWebExchange exchange) {
        if (auth == null || !auth.isAuthenticated()) return Identity.anonymous();

        Object principal = auth.getPrincipal();

        // Case 1: JWT token — claims are directly on the Jwt object
        if (principal instanceof Jwt jwt) {
            String userId = coalesce(jwt.getClaimAsString("userId"), jwt.getSubject(), "");
            String email  = coalesce(jwt.getClaimAsString("email"), "");
            String roles  = rolesFromAuth(auth);
            String tenant = coalesce(jwt.getClaimAsString("tenant_id"), "");
            return new Identity(userId, email, roles, tenant);
        }

        // Case 2: Opaque token — claims are on the OAuth2AuthenticatedPrincipal attributes
        if (auth instanceof BearerTokenAuthentication bearerAuth
                && bearerAuth.getPrincipal() instanceof OAuth2AuthenticatedPrincipal opaqueP) {
            return fromOpaqueAttributes(opaqueP, auth);
        }
        if (principal instanceof OAuth2AuthenticatedPrincipal opaqueP) {
            return fromOpaqueAttributes(opaqueP, auth);
        }

        // Case 3: Opaque token via JwtAuthenticationToken (edge case with JWT that has opaque-like fields)
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userId = coalesce(jwt.getClaimAsString("userId"), jwt.getSubject(), "");
            String email  = coalesce(jwt.getClaimAsString("email"), "");
            String roles  = rolesFromAuth(auth);
            String tenant = coalesce(jwt.getClaimAsString("tenant_id"), "");
            return new Identity(userId, email, roles, tenant);
        }

        // Fallback: build from authorities only
        return new Identity("", "", rolesFromAuth(auth), "");
    }

    private Identity fromOpaqueAttributes(OAuth2AuthenticatedPrincipal principal, Authentication auth) {
        var attrs = principal.getAttributes();
        String userId  = coalesce(str(attrs.get("userId")), str(attrs.get("sub")), principal.getName(), "");
        String email   = coalesce(str(attrs.get("email")), str(attrs.get("username")), "");
        String roles   = rolesFromAuth(auth);
        String tenant  = coalesce(str(attrs.get("tenant_id")), "");
        return new Identity(userId, email, roles, tenant);
    }

    private static String rolesFromAuth(Authentication auth) {
        if (auth == null) return "";
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String coalesce(String... candidates) {
        for (String c : candidates) {
            if (StringUtils.hasText(c)) return c;
        }
        return "";
    }

    // ── Value object ──────────────────────────────────────────────────────────

    private record Identity(String userId, String username, String roles, String tenantId) {
        static Identity anonymous() {
            return new Identity("", "", "", "");
        }
    }
}
