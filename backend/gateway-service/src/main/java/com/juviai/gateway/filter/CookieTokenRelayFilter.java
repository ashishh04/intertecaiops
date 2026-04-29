package com.juviai.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CookieTokenRelayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange == null || exchange.getRequest() == null) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.isBlank()) {
            return chain.filter(exchange);
        }

        String token = extractTokenFromCookies(exchange);
        if (token == null || token.isBlank()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest mutated = request.mutate()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static String extractTokenFromCookies(ServerWebExchange exchange) {
        var cookies = exchange.getRequest().getCookies();
        if (cookies == null || cookies.isEmpty()) return null;

        String[] candidates = {
                "access_token", "accessToken", "ACCESS_TOKEN",
                "id_token", "ID_TOKEN",
                "token", "jwt", "Authorization"
        };

        for (String name : candidates) {
            var c = cookies.getFirst(name);
            if (c == null) continue;
            String v = c.getValue();
            if (v == null || v.isBlank()) continue;

            String trimmed = v.trim();
            if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
                trimmed = trimmed.substring(7).trim();
            }
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }

        return null;
    }
}
