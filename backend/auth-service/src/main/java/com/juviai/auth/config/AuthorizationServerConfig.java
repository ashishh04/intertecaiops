package com.juviai.auth.config;

import java.time.Duration;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

import com.juviai.auth.password.JuviAIPasswordAuthenticationConverter;
import com.juviai.auth.password.JuviAIPasswordAuthenticationProvider;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain checkTokenSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth/check_token")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http,
                                                             OAuth2AuthorizationService authorizationService,
                                                             RegisteredClientRepository registeredClientRepository,
                                                             Customizer<OAuth2AuthorizationServerConfigurer> authorizationServerCustomizer) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        OAuth2AuthorizationServerConfigurer configurer = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        configurer
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(new JuviAIPasswordAuthenticationConverter())
                        .authenticationProvider(new JuviAIPasswordAuthenticationProvider(authorizationService, registeredClientRepository))
                );
        authorizationServerCustomizer.customize(configurer);
        return http.build();
    }

    @Bean
    public Customizer<OAuth2AuthorizationServerConfigurer> authorizationServerCustomizer() {
        return configurer -> {
        };
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
                .requestMatchers("/auth/otp/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**", "/oauth/check_token", "/oauth/dev/**").permitAll()
                .requestMatchers("/.well-known/**", "/oauth2/**").permitAll()
                .requestMatchers("/dev/auth/**").permitAll()
                .requestMatchers("/error", "/error/**").permitAll()
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/auth/register", "/auth/login", "/auth/refresh",
                    "/auth/otp/**",
                    "/dev/auth/**",
                    "/api/auth/**",
                    "/oauth/check_token", "/oauth/dev/**", "/.well-known/**", "/oauth2/**"))
            .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(@NonNull DataSource dataSource) {
        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(@NonNull DataSource dataSource, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), clients);
    }

    @Bean
    public DataSourceInitializer authSchema(@NonNull DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql"));
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql"));
        populator.addScript(new ClassPathResource("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql"));
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public CommandLineRegisteredClientLoader commandLineRegisteredClientLoader(RegisteredClientRepository repo, @NonNull DataSource dataSource) {
        return new CommandLineRegisteredClientLoader(repo, dataSource);
    }

    public static class CommandLineRegisteredClientLoader implements CommandLineRunner {
        private final RegisteredClientRepository repo;
        private final JdbcTemplate jdbcTemplate;

        public CommandLineRegisteredClientLoader(RegisteredClientRepository repo, @NonNull DataSource dataSource) {
            this.repo = repo;
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Override
        public void run(String... args) {
            String clientId = "gateway";

            // Delete existing client and any stale authorizations referencing it.
            // If we delete only from oauth2_registered_client, rows in oauth2_authorization can still reference
            // the old registered_client_id and break /oauth/check_token lookups.
            String existingId = null;
            try {
                existingId = jdbcTemplate.queryForObject(
                        "select id from oauth2_registered_client where client_id = ?",
                        String.class,
                        clientId);
            } catch (Exception ignored) {
            }

            if (existingId != null) {
                jdbcTemplate.update("delete from oauth2_authorization_consent where registered_client_id = ?", existingId);
                jdbcTemplate.update("delete from oauth2_authorization where registered_client_id = ?", existingId);
            }
            jdbcTemplate.update("delete from oauth2_registered_client where client_id = ?", clientId);

            String desiredId = (existingId != null) ? existingId : UUID.randomUUID().toString();
            RegisteredClient desired = RegisteredClient.withId(desiredId)
                    .clientId(clientId)
                    .clientSecret("{noop}gateway-secret")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .authorizationGrantType(new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:juviai-password"))
                    .scope("gateway")
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(15))
                            .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                            .refreshTokenTimeToLive(Duration.ofDays(30))
                            .reuseRefreshTokens(false)
                            .build())
                    .clientSettings(ClientSettings.builder().requireProofKey(false).build())
                    .build();

            ((JdbcRegisteredClientRepository) repo).save(desired);
        }
    }
}
