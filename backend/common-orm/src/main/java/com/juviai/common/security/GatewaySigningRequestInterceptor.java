package com.juviai.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Feign {@link RequestInterceptor} that signs every outgoing inter-service request
 * with the same HMAC-SHA256 gateway signature that the API gateway produces.
 *
 * <h2>Why this is needed</h2>
 * After the security refactor, downstream services only accept requests carrying a
 * valid {@code X-Gateway-Signature} header ({@link GatewayAuthFilter}).  Requests
 * that arrive via the API gateway get this header added automatically.  But
 * service-to-service Feign calls bypass the gateway and therefore need to add the
 * same signature themselves — that is exactly what this interceptor does.
 *
 * <h2>How it works</h2>
 * The interceptor reads the current {@code SecurityContext} (which was populated by
 * {@link GatewayAuthFilter} when the incoming HTTP request was processed), extracts
 * the user identity, and re-signs the outgoing Feign request with the shared secret.
 * The receiving service then verifies the signature exactly as if the request had
 * come from the gateway.
 *
 * <h2>Correlation ID propagation</h2>
 * The interceptor also forwards the {@code X-Correlation-ID} header from MDC so
 * that a single business transaction can be traced across service boundaries.
 *
 * <h2>Registration</h2>
 * This bean is declared in {@link GatewayFeignAutoConfig} and picked up automatically
 * by Spring Cloud Feign as a global interceptor for all {@code @FeignClient} interfaces
 * in any service that scans {@code com.juviai.common}.
 */
public class GatewaySigningRequestInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GatewaySigningRequestInterceptor.class);

    /** MDC key used by {@link com.juviai.common.web.CorrelationIdFilter}. */
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private final GatewaySecurityProperties props;

    public GatewaySigningRequestInterceptor(GatewaySecurityProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RequestTemplate template) {

        // ── 1. Extract identity from current SecurityContext ─────────────────────
        String userId   = "";
        String roles    = "";
        String username = "";
        String tenantId = "";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            userId   = attr(principal, "userId");
            username = attr(principal, "username");
            tenantId = attr(principal, "tenant_id");
            roles    = auth.getAuthorities().stream()
                           .map(GrantedAuthority::getAuthority)
                           .collect(Collectors.joining(","));
        }

        // ── 2. Derive path from Feign URL (strip host + query) ───────────────────
        String path = extractPath(template.url());

        // ── 3. Sign and attach gateway headers ───────────────────────────────────
        long epochSec  = Instant.now().getEpochSecond();
        String signature = GatewayHmacUtil.sign(
                props.getSharedSecret(), userId, roles, path, epochSec);

        template.header(GatewayHmacUtil.HEADER_SIGNATURE, signature);
        template.header(GatewayHmacUtil.HEADER_TIMESTAMP,  String.valueOf(epochSec));
        template.header(GatewayHmacUtil.HEADER_USER_ID,    userId);
        template.header(GatewayHmacUtil.HEADER_ROLES,      roles);
        template.header(GatewayHmacUtil.HEADER_USERNAME,   username);
        template.header(GatewayHmacUtil.HEADER_TENANT_ID,  tenantId);

        // ── 4. Forward correlation ID for distributed tracing ────────────────────
        String correlationId = org.slf4j.MDC.get(MDC_CORRELATION_ID);
        if (correlationId != null && !correlationId.isBlank()) {
            template.header(HEADER_CORRELATION_ID, correlationId);
        }

        log.debug("Signed inter-service Feign request: path={} userId={}", path, userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String attr(OAuth2AuthenticatedPrincipal principal, String key) {
        Object val = principal.getAttribute(key);
        return val == null ? "" : String.valueOf(val);
    }

    /**
     * Extract the URI path from a Feign template URL.
     * The URL may be a full URL ({@code http://wallet-service/api/payments/initiate})
     * or just a path ({@code /api/payments/initiate}).
     */
    private static String extractPath(String url) {
        if (url == null || url.isBlank()) return "/";
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            return (path == null || path.isBlank()) ? "/" : path;
        } catch (IllegalArgumentException e) {
            // Fallback: treat the whole string as a path
            int q = url.indexOf('?');
            return q >= 0 ? url.substring(0, q) : url;
        }
    }
}
