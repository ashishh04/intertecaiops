package com.juviai.gateway.filter;

import com.juviai.gateway.config.GatewayProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(prefix = "juviai.gateway.introspection", name = "enabled", havingValue = "true")
public class TokenIntrospectionCacheFilter implements GlobalFilter {

    private final WebClient webClient;
    private final StringRedisTemplate redis;
    private final GatewayProperties props;

    public TokenIntrospectionCacheFilter(WebClient webClient, StringRedisTemplate redis, GatewayProperties props) {
        this.webClient = webClient;
        this.redis = redis;
        this.props = props;
    }

    @SuppressWarnings("null")
	@Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.toLowerCase().startsWith("bearer ")) {
            return chain.filter(exchange);
        }
        String token = auth.substring(7).trim();
        String tenantCandidate = resolveTenant(request);
        final String resolvedTenant = (StringUtils.hasText(tenantCandidate)) ? tenantCandidate : "default";
        final String cacheKey = "tenant:" + resolvedTenant + ":check_token:" + token;
        if (!props.isStrictRevocation()) {
            String cached = redis.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cached)) {
                ServerHttpRequest mutated = request.mutate()
                        .header("X-Principal-Claims", cached)
                        .header("X-JuviAI-Tenant", resolvedTenant)
                        .build();
                return chain.filter(exchange.mutate().request(mutated).build());
            }
        }
        // Cache miss or strict mode
        return webClient.post()
                .uri(props.getIntrospectionUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(props.getClientId(), props.getClientSecret(), StandardCharsets.UTF_8))
                .body(BodyInserters.fromFormData("token", token))
                .retrieve()
                .bodyToMono(String.class)
                .defaultIfEmpty("{\"active\":false}")
                .flatMap(body -> {
                    // Only cache active tokens. Caching {"active":false} would cause revoked or
                    // expired tokens to continue passing through until the TTL expires, creating
                    // a security window. Invalid/inactive tokens are always re-introspected.
                    if (!props.isStrictRevocation() && isActiveToken(body)) {
                        redis.opsForValue().set(cacheKey, body, Duration.ofSeconds(props.getCheckTokenCacheTtlSeconds()));
                    }
                    ServerHttpRequest mutated = request.mutate()
                            .header("X-Principal-Claims", body)
                            .header("X-JuviAI-Tenant", resolvedTenant)
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                });
    }

    /**
     * Returns {@code true} when the introspection response body contains {@code "active":true}.
     * A simple contains-check is sufficient here — we own the auth-server response format
     * and avoid pulling in a JSON library just for this one field.
     */
    private static boolean isActiveToken(String body) {
        if (!StringUtils.hasText(body)) return false;
        // Match both "active":true and "active": true (with optional whitespace)
        return body.contains("\"active\":true") || body.contains("\"active\": true");
    }

    private String resolveTenant(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("X-JuviAI-Tenant");
        if (StringUtils.hasText(header)) return header;
        String host = request.getHeaders().getFirst("Host");
        if (host != null && host.endsWith(props.getBaseDomain())) {
            String sub = host.substring(0, host.length() - props.getBaseDomain().length());
            if (sub.endsWith(".")) sub = sub.substring(0, sub.length() - 1);
            if (StringUtils.hasText(sub)) return sub;
        }
        return null;
    }
}
