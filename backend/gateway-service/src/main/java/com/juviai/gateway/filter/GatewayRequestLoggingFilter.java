package com.juviai.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import reactor.core.publisher.Mono;

/**
 * Logs gateway routing decisions and downstream failures.
 *
 * This is intentionally minimal but extremely useful when a route returns 5xx and
 * the downstream service shows no logs (meaning the request likely never reached it).
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class GatewayRequestLoggingFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayRequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();

        String method = req.getMethod() != null ? req.getMethod().name() : "";
        String path = req.getURI() != null ? req.getURI().getPath() : "";
        String requestId = exchange.getRequest().getId();

        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    ServerHttpResponse resp = exchange.getResponse();
                    HttpStatusCode status = resp.getStatusCode();
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "";
                    String uri = route != null ? String.valueOf(route.getUri()) : "";
                    int code = status != null ? status.value() : 0;

                    if (code >= 400) {
                        log.warn("GW {} {} -> {} routeId={} uri={} requestId={}", method, path, code, routeId, uri, requestId);
                    } else {
                        log.debug("GW {} {} -> {} routeId={} uri={} requestId={}", method, path, code, routeId, uri, requestId);
                    }
                })
                .doOnError(ex -> {
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "";
                    String uri = route != null ? String.valueOf(route.getUri()) : "";
                    log.error("GW {} {} -> ERROR routeId={} uri={} requestId={} ex={} msg={}",
                            method, path, routeId, uri, requestId,
                            ex.getClass().getSimpleName(), String.valueOf(ex.getMessage()), ex);
                });
    }
}
