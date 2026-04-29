package com.juviai.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:}")
    private String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret:}")
    private String clientSecret;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    private static boolean looksLikeJwt(String token) {
        if (token == null) return false;

        String t = token.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1).trim();
        }

        String[] parts = t.split("\\.");
        if (parts.length != 3) return false;
        if (parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) return false;

        // base64url-ish check
        if (!parts[0].matches("[A-Za-z0-9_-]+") || !parts[1].matches("[A-Za-z0-9_-]+")) return false;

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[0]);
            String header = new String(decoded, StandardCharsets.UTF_8);
            // minimal sanity: JWT header is JSON object
            return header.startsWith("{") && header.contains("\"alg\"");
        } catch (Exception e) {
            return false;
        }
    }

    private static Collection<GrantedAuthority> authoritiesFromRoles(Object rolesObj) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (rolesObj == null) return authorities;

        if (rolesObj instanceof String s) {
            addRoleAuthority(authorities, s);
            return authorities;
        }
        if (rolesObj instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null) addRoleAuthority(authorities, String.valueOf(o));
            }
            return authorities;
        }

        addRoleAuthority(authorities, String.valueOf(rolesObj));
        return authorities;
    }

    private static Collection<GrantedAuthority> authoritiesFromScope(Object scopeObj) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (scopeObj == null) return authorities;

        if (scopeObj instanceof String s) {
            for (String token : s.split("\\s+")) {
                addScopeAuthorities(authorities, token);
            }
            return authorities;
        }
        if (scopeObj instanceof Collection<?> c) {
            for (Object o : c) {
                if (o == null) continue;
                addScopeAuthorities(authorities, String.valueOf(o));
            }
            return authorities;
        }

        addScopeAuthorities(authorities, String.valueOf(scopeObj));
        return authorities;
    }

    private static void addScopeAuthorities(Set<GrantedAuthority> authorities, String scope) {
        if (scope == null) return;
        String s = scope.trim();
        if (s.isBlank()) return;

        // Spring's default is SCOPE_x; this gateway previously logged ROLE_SCOPE_gateway
        // so we support both.
        authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
        authorities.add(new SimpleGrantedAuthority("ROLE_SCOPE_" + s));
    }

    private static Collection<GrantedAuthority> authoritiesFromClaims(Map<String, Object> claims) {
        Set<GrantedAuthority> out = new HashSet<>();
        if (claims == null || claims.isEmpty()) return out;

        // Common role claims
        out.addAll(authoritiesFromRoles(claims.get("roles")));
        out.addAll(authoritiesFromRoles(claims.get("authorities")));
        out.addAll(authoritiesFromRoles(claims.get("role")));

        // Common scope claims
        out.addAll(authoritiesFromScope(claims.get("scope")));
        out.addAll(authoritiesFromScope(claims.get("scp")));

        // Keycloak-style realm roles: { "realm_access": { "roles": [..] } }
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> ra) {
            Object rr = ra.get("roles");
            out.addAll(authoritiesFromRoles(rr));
        }

        // Keycloak-style resource roles:
        // { "resource_access": { "<client>": { "roles": [..] } } }
        Object resourceAccess = claims.get("resource_access");
        if (resourceAccess instanceof Map<?, ?> rmap) {
            for (Object clientEntry : rmap.values()) {
                if (!(clientEntry instanceof Map<?, ?> c)) continue;
                Object cr = c.get("roles");
                out.addAll(authoritiesFromRoles(cr));
            }
        }

        // Skillrat scoped roles — the authorization server may emit a claim
        // "scoped_roles" containing entries of the form:
        //   "ROLE_STORE_ADMIN@STORE:<uuid>"
        //   "ROLE_BUSINESS_ADMIN@B2B_UNIT:<uuid>"
        //   "ROLE_PROJECT_MANAGER@PROJECT:<uuid>"
        //   "ROLE_ADMIN@GLOBAL"            (no ":id" for GLOBAL)
        // These are emitted verbatim so they match exactly what
        // ScopedSecurity.authoritiesContainScopedRole() expects — the fast
        // authority-based check then short-circuits the DB fallback.
        out.addAll(authoritiesFromScopedRoles(claims.get("scoped_roles")));
        out.addAll(authoritiesFromScopedRoles(claims.get("scopedRoles")));

        return out;
    }

    /**
     * Parse a {@code scoped_roles} claim (string or collection of strings) and
     * emit one {@link GrantedAuthority} per entry, preserving the exact
     * {@code ROLE@SCOPE:id} form so {@code ScopedSecurity.has(...)} matches
     * without a DB lookup. Malformed entries are skipped silently.
     */
    private static Collection<GrantedAuthority> authoritiesFromScopedRoles(Object scopedRolesObj) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (scopedRolesObj == null) return authorities;

        if (scopedRolesObj instanceof String s) {
            addScopedRoleAuthority(authorities, s);
            return authorities;
        }
        if (scopedRolesObj instanceof Collection<?> c) {
            for (Object o : c) {
                if (o == null) continue;
                addScopedRoleAuthority(authorities, String.valueOf(o));
            }
            return authorities;
        }

        addScopedRoleAuthority(authorities, String.valueOf(scopedRolesObj));
        return authorities;
    }

    private static void addScopedRoleAuthority(Set<GrantedAuthority> authorities, String raw) {
        if (raw == null) return;
        String v = raw.trim();
        if (v.isBlank()) return;
        // Must contain the "@" separator that ScopedSecurity expects; otherwise
        // it's a plain role and we route through the role-authority path.
        int at = v.indexOf('@');
        if (at < 1 || at >= v.length() - 1) {
            addRoleAuthority(authorities, v);
            return;
        }
        String role = v.substring(0, at);
        String rolePrefixed = role.startsWith("ROLE_") ? role : ("ROLE_" + role);
        String rest = v.substring(at + 1);
        // Normalise the role portion so gateway and downstream agree on form.
        String canonical = rolePrefixed + "@" + rest;
        authorities.add(new SimpleGrantedAuthority(canonical));
        // Also expose the bare role for path-matcher style "hasRole(X)" checks.
        authorities.add(new SimpleGrantedAuthority(rolePrefixed));
    }

    private static ServerAccessDeniedHandler accessDeniedLogger() {
        ServerAccessDeniedHandler delegate = new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN);
        return (exchange, denied) -> exchange.getPrincipal()
                .ofType(Authentication.class)
                .defaultIfEmpty(new org.springframework.security.authentication.AnonymousAuthenticationToken(
                        "anonymous",
                        "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))))
                .flatMap(auth -> {
                    String path = exchange != null && exchange.getRequest() != null && exchange.getRequest().getURI() != null
                            ? exchange.getRequest().getURI().getPath()
                            : "<unknown>";
                    log.warn(
                            "Gateway access denied path={} principal={} authorities={} message={}",
                            path,
                            auth.getName(),
                            auth.getAuthorities(),
                            denied != null ? denied.getMessage() : null);
                    return delegate.handle(exchange, denied);
                });
    }

    private static void addRoleAuthority(Set<GrantedAuthority> authorities, String role) {
        if (role == null) return;
        String name = role.trim();
        if (name.isBlank()) return;

        String normalized = name.startsWith("ROLE_") ? name : ("ROLE_" + name);
        if (normalized.startsWith("ROLE_BUSINESS_ADMIN")) {
            normalized = "ROLE_BUSINESS_ADMIN";
        }
        authorities.add(new SimpleGrantedAuthority(normalized));
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    CorsWebFilter corsWebFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsWebFilter(corsConfigurationSource);
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {

        String effectiveJwkSetUri = (jwkSetUri == null || jwkSetUri.isBlank())
                ? "http://localhost:8087/oauth2/jwks"
                : jwkSetUri;
        String effectiveIntrospectionUri = (introspectionUri == null || introspectionUri.isBlank())
                ? "http://localhost:8087/oauth/check_token"
                : introspectionUri;

        ServerAuthenticationConverter bearer = exchange -> {
            ServerBearerTokenAuthenticationConverter delegate = new ServerBearerTokenAuthenticationConverter();
            return delegate.convert(exchange)
                    .switchIfEmpty(Mono.defer(() -> {
                        String token = extractBearerFromCookies(exchange);
                        if (token == null || token.isBlank()) {
                            return Mono.empty();
                        }
                        return Mono.just(new BearerTokenAuthenticationToken(token));
                    }));
        };

        ReactiveAuthenticationManager jwtAuthManager;
        if (effectiveJwkSetUri != null && !effectiveJwkSetUri.isBlank()) {
            ReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(effectiveJwkSetUri).build();

            JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
            jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> authoritiesFromClaims(jwt.getClaims()));

            JwtReactiveAuthenticationManager jwtManager = new JwtReactiveAuthenticationManager(jwtDecoder);
            jwtManager.setJwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(jwtConverter));
            jwtAuthManager = jwtManager;
        } else {
            jwtAuthManager = authentication -> {
                throw new AuthenticationServiceException("JWT is not configured (missing jwk-set-uri)");
            };
        }

        ReactiveAuthenticationManager opaqueAuthManager;
        if (effectiveIntrospectionUri != null && !effectiveIntrospectionUri.isBlank()) {
            ReactiveOpaqueTokenIntrospector delegateIntrospector =
                    new NimbusReactiveOpaqueTokenIntrospector(effectiveIntrospectionUri, clientId, clientSecret);
            ReactiveOpaqueTokenIntrospector introspector = token -> delegateIntrospector.introspect(token)
                    .map(principal -> {
                        Map<String, Object> attributes = principal.getAttributes();
                        Collection<GrantedAuthority> authorities = new HashSet<>(principal.getAuthorities());
                        authorities.addAll(authoritiesFromClaims(attributes));
                        return new OAuth2IntrospectionAuthenticatedPrincipal(attributes, authorities);
                    });
            opaqueAuthManager = new OpaqueTokenReactiveAuthenticationManager(introspector);
        } else {
            opaqueAuthManager = authentication -> {
                throw new AuthenticationServiceException("Opaque token introspection is not configured (missing introspection-uri)");
            };
        }

        ReactiveAuthenticationManager combinedAuthManager = authentication -> {
            if (!(authentication instanceof BearerTokenAuthenticationToken bearerAuth)) {
                return Mono.error(new BadCredentialsException("Unsupported authentication type"));
            }

            String token = bearerAuth.getToken();
            boolean preferJwt = looksLikeJwt(token);

            ReactiveAuthenticationManager first = preferJwt ? jwtAuthManager : opaqueAuthManager;
            ReactiveAuthenticationManager second = preferJwt ? opaqueAuthManager : jwtAuthManager;

            return first.authenticate(authentication)
                    .onErrorResume(firstErr -> {
                        if (!preferJwt && !(firstErr instanceof AuthenticationServiceException)) {
                            log.warn(
                                    "Opaque token authentication failed (no JWT fallback). error={}({})",
                                    firstErr.getClass().getSimpleName(),
                                    String.valueOf(firstErr.getMessage()));
                            return Mono.error(firstErr);
                        }

                        return second.authenticate(authentication)
                                .onErrorResume(secondErr -> {
                                    log.warn(
                                            "Bearer token authentication failed (preferJwt={}): first={}({}), second={}({})",
                                            preferJwt,
                                            firstErr.getClass().getSimpleName(),
                                            String.valueOf(firstErr.getMessage()),
                                            secondErr.getClass().getSimpleName(),
                                            String.valueOf(secondErr.getMessage()));
                                    return Mono.error(secondErr);
                                });
                    });
        };

        ReactiveAuthenticationManagerResolver<ServerWebExchange> resolver = exchange -> bearer.convert(exchange)
                .map(auth -> combinedAuthManager)
                .switchIfEmpty(Mono.empty());

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(eh -> eh.accessDeniedHandler(accessDeniedLogger()))
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/api/users/login", "/api/users/signup", "/api/users/password/reset", "/api/users/password/setup").permitAll()
                        .pathMatchers("/api/cities", "/api/cities/**").permitAll()
                        .pathMatchers("/api/b2b/search").permitAll()
                        .pathMatchers("/api/languages", "/api/languages/**").permitAll()
                        .pathMatchers("/api/openings/public/**", "/api/openings/*/apply", "/api/openings/*").permitAll()
                        .pathMatchers("/api/appointments/admin/**").hasRole("BUSINESS_ADMIN")
                        .pathMatchers("/api/appointments/**").authenticated()
                        .pathMatchers("/api/orders/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_BUSINESS_ADMIN")
                        .pathMatchers("/api/orders/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(resolver))
                .build();
    }

    private static String extractBearerFromCookies(ServerWebExchange exchange) {
        if (exchange == null || exchange.getRequest() == null) return null;
        var cookies = exchange.getRequest().getCookies();
        if (cookies == null || cookies.isEmpty()) return null;

        String token = null;

        String[] candidates = {
                "access_token", "accessToken", "ACCESS_TOKEN",
                "id_token", "ID_TOKEN",
                "token", "jwt", "Authorization"
        };
        for (String name : candidates) {
            var c = cookies.getFirst(name);
            if (c == null) continue;
            String v = c.getValue();
            if (v != null && !v.isBlank()) {
                token = v;
                break;
            }
        }
        if (token == null) return null;

        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }
}
