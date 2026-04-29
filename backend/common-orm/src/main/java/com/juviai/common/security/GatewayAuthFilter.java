package com.juviai.common.security;

import com.juviai.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet filter that authenticates requests forwarded by the API gateway.
 *
 * <h2>Authentication flow</h2>
 * <ol>
 *   <li>Reads {@code X-Gateway-Signature} and {@code X-Gateway-Timestamp} headers.</li>
 *   <li>Verifies the HMAC-SHA256 signature using the shared secret from
 *       {@link GatewaySecurityProperties}.</li>
 *   <li>If valid: builds a {@link UsernamePasswordAuthenticationToken} from the propagated
 *       {@code X-User-Id} / {@code X-Roles} / {@code X-Username} / {@code X-Tenant-Id} headers
 *       and sets it in the {@link SecurityContextHolder}.</li>
 *   <li>If invalid and {@code enforceSignature=true}: returns {@code 401 Unauthorized}
 *       immediately — the request never reaches the controller.</li>
 *   <li>If invalid and {@code enforceSignature=false}: logs a warning and continues the
 *       filter chain to allow plain Bearer-token access during local development.</li>
 * </ol>
 *
 * <h2>Header injection prevention</h2>
 * The gateway's {@code RequestSanitizationFilter} strips all
 * {@code X-Gateway-*}, {@code X-User-Id}, {@code X-Roles}, and {@code X-Tenant-Id}
 * headers from every inbound client request <em>before</em> authentication, so clients
 * can never forge these headers.  The HMAC provides the mathematical proof that only the
 * gateway (which holds the shared secret) could have produced the signature.
 *
 * <h2>Registration</h2>
 * This filter must be registered <em>before</em>
 * {@code UsernamePasswordAuthenticationFilter} in each service's {@code SecurityConfig}.
 * Because it is defined in {@code common-orm}, every service simply declares:
 * <pre>
 *   http.addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);
 * </pre>
 */
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class);

    private final GatewaySecurityProperties props;

    public GatewayAuthFilter(GatewaySecurityProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String signature  = request.getHeader(GatewayHmacUtil.HEADER_SIGNATURE);
        String tsHeader   = request.getHeader(GatewayHmacUtil.HEADER_TIMESTAMP);
        String userId     = trimOrEmpty(request.getHeader(GatewayHmacUtil.HEADER_USER_ID));
        String roles      = trimOrEmpty(request.getHeader(GatewayHmacUtil.HEADER_ROLES));
        String username   = trimOrEmpty(request.getHeader(GatewayHmacUtil.HEADER_USERNAME));
        String tenantId   = trimOrEmpty(request.getHeader(GatewayHmacUtil.HEADER_TENANT_ID));

        // No gateway signature present at all
        if (signature == null || tsHeader == null) {
            if (props.isEnforceSignature()) {
                log.warn("Missing gateway signature headers for {} {}", request.getMethod(), request.getRequestURI());
                reject(response, request, "Missing gateway signature headers");
                return;
            }
            // Non-strict mode: allow the rest of the security chain to handle it (dev/testing)
            chain.doFilter(request, response);
            return;
        }

        // Parse timestamp
        long timestampSec;
        try {
            timestampSec = Long.parseLong(tsHeader.trim());
        } catch (NumberFormatException e) {
            log.warn("Malformed X-Gateway-Timestamp for {} {}", request.getMethod(), request.getRequestURI());
            reject(response, request, "Malformed X-Gateway-Timestamp");
            return;
        }

        // Verify HMAC
        boolean valid = GatewayHmacUtil.verify(
                props.getSharedSecret(),
                signature,
                userId,
                roles,
                request.getRequestURI(),
                timestampSec,
                props.getTimestampToleranceSeconds());

        if (!valid) {
            log.warn("Gateway HMAC verification failed for {} {}", request.getMethod(), request.getRequestURI());
            reject(response, request, "Invalid or expired gateway signature");
            return;
        }

        // Signature is valid — build SecurityContext from propagated headers
        Collection<GrantedAuthority> authorities = parseRoles(roles);

        Map<String, Object> attributes = new HashMap<>();
        if (!userId.isBlank())    attributes.put("userId",    userId);
        if (!username.isBlank())  attributes.put("username",  username);
        if (!tenantId.isBlank())  attributes.put("tenant_id", tenantId);
        if (!roles.isBlank())     attributes.put("roles",     roles);

        String principalName = userId.isBlank() ? "anonymous" : userId;
        OAuth2AuthenticatedPrincipal principal =
                new DefaultOAuth2AuthenticatedPrincipal(principalName, attributes, authorities);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        auth.setDetails(attributes);

        SecurityContextHolder.getContext().setAuthentication(auth);

        // Propagate tenant to ThreadLocal for JPA auditing / multi-tenancy
        if (!tenantId.isBlank()) {
            TenantContext.setTenantId(tenantId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void reject(HttpServletResponse response, HttpServletRequest request, String reason) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        String instance = request != null ? request.getRequestURI() : "";
        response.getWriter().write(
                "{" +
                        "\"type\":\"https://skillrat.errors/unauthorized\"," +
                        "\"title\":\"Unauthorized\"," +
                        "\"status\":401," +
                        "\"detail\":" + jsonString(reason) + "," +
                        "\"instance\":" + jsonString(instance) + "," +
                        "\"timestamp\":" + jsonString(Instant.now().toString()) +
                "}");
    }

    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static Collection<GrantedAuthority> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) return List.of();
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : roles.split(",")) {
            String r = role.trim();
            if (r.isBlank()) continue;
            String name = r.startsWith("ROLE_") ? r : ("ROLE_" + r);
            authorities.add(new SimpleGrantedAuthority(name));

            if (name.startsWith("ROLE_BUSINESS_ADMIN_")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_BUSINESS_ADMIN"));
            }
        }
        return new ArrayList<>(authorities);
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
