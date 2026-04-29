package com.juviai.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Gateway WebFilter that ensures every request flowing through the API gateway
 * carries a {@code X-Correlation-ID} header.
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>If the incoming client request already carries {@code X-Correlation-ID},
 *       that value is forwarded unchanged.</li>
 *   <li>If the header is absent, a new UUID is generated and injected into the
 *       request before it is forwarded to a downstream service.</li>
 *   <li>The resolved correlation ID is always echoed in the outbound response so
 *       callers can log it for support purposes.</li>
 * </ul>
 *
 * <h2>Ordering</h2>
 * Runs at {@code HIGHEST_PRECEDENCE + 5} — after {@link RequestSanitizationFilter}
 * (which strips attacker-supplied headers) but before all other business filters.
 * This ordering guarantees that the correlation ID is available to every subsequent
 * filter in the chain.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    /** Runs just after RequestSanitizationFilter (HIGHEST_PRECEDENCE) */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 5;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        List<String> existing = exchange.getRequest().getHeaders().get(HEADER_CORRELATION_ID);
        String correlationId = (existing != null && !existing.isEmpty() && !existing.get(0).isBlank())
                ? existing.get(0)
                : UUID.randomUUID().toString();

        log.debug("Request correlationId={} path={}",
                correlationId, exchange.getRequest().getPath().value());

        // Inject into the request forwarded to downstream services
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER_CORRELATION_ID, correlationId)
                .build();

        // Echo in the response for the original caller
        exchange.getResponse().getHeaders().set(HEADER_CORRELATION_ID, correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
