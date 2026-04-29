package com.juviai.user.config;

import com.juviai.common.security.GatewayAuthFilter;
import com.juviai.common.security.GatewaySecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * User-service security configuration.
 *
 * <h2>Authentication model</h2>
 * All authentication is performed once at the API gateway.  The gateway validates
 * the bearer token, signs the forwarded request with HMAC-SHA256, and propagates
 * identity as trusted headers ({@code X-User-Id}, {@code X-Roles}, etc.).
 *
 * <p>{@link GatewayAuthFilter} verifies the HMAC signature and builds the
 * {@code SecurityContext} — no further token introspection happens here.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public GatewayAuthFilter gatewayAuthFilter(GatewaySecurityProperties props) {
        return new GatewayAuthFilter(props);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   GatewayAuthFilter gatewayAuthFilter) throws Exception {
        http
            .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Public auth endpoints — no token required
                .requestMatchers("/api/users/login", "/api/users/signup",
                                 "/api/users/password/reset", "/api/users/password/setup").permitAll()
                // Internal service-to-service endpoints
                .requestMatchers("/api/users/internal/**").permitAll()
                // Public lookup endpoints
                .requestMatchers("/api/cities", "/api/cities/**").permitAll()
                .requestMatchers("/api/b2b/search").permitAll()
                // Admin data imports require authentication (role checked via @PreAuthorize)
                .requestMatchers(HttpMethod.POST, "/api/states/admin/import-excel").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/cities/admin/import-excel").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
