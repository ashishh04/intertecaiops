package com.juviai.gateway.filter;

import com.juviai.common.security.GatewayHmacUtil;
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

import java.util.stream.Collectors;

/**
 * Extracts identity claims from the authenticated principal and propagates them
 * as request headers to downstream services.
 *
 * <p>Handles BOTH token types:
 * <ul>
 *   <li><strong>JWT</strong> — claims read from {@link Jwt} object</li>
 *   <li><strong>Opaque</strong> — claims read from
 *       {@link OAuth2AuthenticatedPrincipal#getAttributes()} returned by introspection</li>
 * </ul>
 *
 * <p>{@link GatewaySignatureFilter} runs after this filter (HIGHEST+25) and
 * re-reads the same headers when building the HMAC payload, so the propagated
 * values and the signature are always consistent.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class ClaimsPropagationFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    Claims claims = extractClaims(auth);
                    if (claims == null) return chain.filter(exchange);

                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
                    if (StringUtils.hasText(claims.userId()))   builder.header(GatewayHmacUtil.HEADER_USER_ID,  claims.userId());
                    if (StringUtils.hasText(claims.username())) builder.header(GatewayHmacUtil.HEADER_USERNAME, claims.username());
                    if (StringUtils.hasText(claims.roles()))    builder.header(GatewayHmacUtil.HEADER_ROLES,    claims.roles());

                    return chain.filter(exchange.mutate().request(builder.build()).build());
                })
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
    }

    // ── Claim extraction — supports JWT and opaque tokens ─────────────────────

    private Claims extractClaims(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();

        // JWT token (JwtAuthenticationToken or principal is Jwt directly)
        if (principal instanceof Jwt jwt) {
            return new Claims(
                    coalesce(jwt.getClaimAsString("userId"), jwt.getSubject()),
                    coalesce(jwt.getClaimAsString("email"), ""),
                    rolesFrom(auth));
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return new Claims(
                    coalesce(jwt.getClaimAsString("userId"), jwt.getSubject()),
                    coalesce(jwt.getClaimAsString("email"), ""),
                    rolesFrom(auth));
        }

        // Opaque token — BearerTokenAuthentication wraps OAuth2AuthenticatedPrincipal
        if (auth instanceof BearerTokenAuthentication bearerAuth
                && bearerAuth.getPrincipal() instanceof OAuth2AuthenticatedPrincipal opaqueP) {
            return fromOpaque(opaqueP, auth);
        }
        if (principal instanceof OAuth2AuthenticatedPrincipal opaqueP) {
            return fromOpaque(opaqueP, auth);
        }

        return null;
    }

    private Claims fromOpaque(OAuth2AuthenticatedPrincipal principal, Authentication auth) {
        var attrs = principal.getAttributes();
        return new Claims(
                coalesce(str(attrs.get("userId")), str(attrs.get("sub")), principal.getName()),
                coalesce(str(attrs.get("email")), str(attrs.get("username")), ""),
                rolesFrom(auth));
    }

    private static String rolesFrom(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));
    }

    private static String str(Object o)       { return o == null ? null : String.valueOf(o); }
    private static String coalesce(String... v) {
        for (String s : v) if (StringUtils.hasText(s)) return s;
        return "";
    }

    private record Claims(String userId, String username, String roles) {}
}
