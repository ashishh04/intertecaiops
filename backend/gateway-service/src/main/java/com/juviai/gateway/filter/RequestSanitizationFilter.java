package com.juviai.gateway.filter;

import com.juviai.common.security.GatewayHmacUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Strips all security-sensitive headers that a client could use to impersonate
 * a trusted gateway or inject a fake identity.
 *
 * <p>This filter runs at the <strong>highest precedence</strong> — before token
 * extraction, authentication, and any other filter — so no attacker-supplied
 * header ever reaches downstream services.
 *
 * <p>Headers removed from every inbound client request:
 * <ul>
 *   <li>{@code X-Gateway-Signature} and {@code X-Gateway-Timestamp} —
 *       only the gateway itself should produce these after authentication.</li>
 *   <li>{@code X-User-Id}, {@code X-Roles}, {@code X-Username} —
 *       only the gateway may propagate these after validating the bearer token.</li>
 *   <li>{@code X-Tenant-Id} —
 *       only the gateway resolves tenant from the subdomain/header.</li>
 *   <li>{@code X-Internal-Token} —
 *       reserved for inter-service calls; never accepted from external clients.</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestSanitizationFilter implements GlobalFilter {

    /** Headers that clients must never supply — they are gateway-generated only. */
    private static final String[] PROTECTED_HEADERS = {
            GatewayHmacUtil.HEADER_SIGNATURE,
            GatewayHmacUtil.HEADER_TIMESTAMP,
            GatewayHmacUtil.HEADER_USER_ID,
            GatewayHmacUtil.HEADER_ROLES,
            GatewayHmacUtil.HEADER_USERNAME,
            GatewayHmacUtil.HEADER_TENANT_ID,
            "X-Internal-Token",
            "X-Principal-Claims",     // legacy header used by older TenantTokenValidationFilter
            // Strip client-supplied correlation IDs — the gateway assigns its own
            // so clients cannot forge correlation IDs to confuse log aggregation.
            "X-Correlation-ID",
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(headers -> {
                    for (String header : PROTECTED_HEADERS) {
                        headers.remove(header);
                    }
                })
                .build();
        return chain.filter(exchange.mutate().request(sanitized).build());
    }
}
