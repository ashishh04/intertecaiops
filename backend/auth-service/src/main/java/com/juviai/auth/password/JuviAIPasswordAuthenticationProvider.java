package com.juviai.auth.password;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.client.RestTemplate;

import com.juviai.common.tenant.TenantContext;

public class JuviAIPasswordAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(JuviAIPasswordAuthenticationProvider.class);
    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository clientRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom secureRandom = new SecureRandom();

    @org.springframework.beans.factory.annotation.Value("${juviai.user.base-url:http://localhost:8081}")
    private String userBaseUrl;

    public static final AuthorizationGrantType GRANT_TYPE = new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:juviai-password");

    public JuviAIPasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                                  RegisteredClientRepository clientRepository) {
        this.authorizationService = authorizationService;
        this.clientRepository = clientRepository;
    }

    @SuppressWarnings({ "rawtypes", "null" })
	@Override
    public Authentication authenticate(Authentication authentication) {
        if (!(authentication instanceof JuviAIPasswordAuthenticationToken tokenRequest)) {
            return null;
        }
        // Extract client
        Authentication clientPrincipal = tokenRequest.getClientPrincipal();
        Object clientIdAttr = clientPrincipal.getName();
        RegisteredClient registeredClient = clientRepository.findByClientId(String.valueOf(clientIdAttr));
        if (registeredClient == null || !registeredClient.getAuthorizationGrantTypes().contains(GRANT_TYPE)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unauthorized_client", "Client not authorized for juviai-password grant", null));
        }

        // Validate credentials against user-service
        String username = tokenRequest.getUsername();
        String password = tokenRequest.getPassword();
        if (username == null || password == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request", "username and password are required", null));
        }
        Map<String, String> body = new HashMap<>();
        body.put("emailOrMobile", username);
        body.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Propagate tenant header if present in SecurityContext? Not readily available; default to 'default'
        String tenant = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : "default";
        headers.add("X-JuviAI-Tenant", tenant);
        ResponseEntity<Map> resp = restTemplate.exchange(userBaseUrl + "/api/users/login", HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("access_denied", "Invalid credentials", null));
        }
        Map<?,?> userInfo = resp.getBody();
        Object emailObj = userInfo != null ? userInfo.get("email") : null;
        String principalName = emailObj != null ? emailObj.toString() : username;

        java.util.List<?> rolesList = userInfo != null ? (java.util.List<?>) userInfo.get("roles") : java.util.List.of();
        java.util.List<SimpleGrantedAuthority> auths = new java.util.ArrayList<>();
        if (rolesList != null) {
            for (Object r : rolesList) {
                if (r != null) auths.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
            }
        }
        if (auths.isEmpty()) {
            auths = java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        final java.util.List<String> roleNames = auths.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .filter(a -> a != null && a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .toList();
        final java.util.List<String> safeRoleNames = new ArrayList<>(roleNames);
        final String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : "default";
        // Build authorization and generate opaque access token
        Set<String> authorizedScopes = registeredClient.getScopes();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(java.time.Duration.ofDays(30));
        String tokenValue = generateTokenValue();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenValue,
                issuedAt,
                expiresAt,
                authorizedScopes);

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(principalName)
                .authorizationGrantType(GRANT_TYPE)
                .authorizedScopes(authorizedScopes)
                .attribute(OAuth2ParameterNames.USERNAME, principalName)
                .token(accessToken, metadata -> {
                    metadata.put("sub", principalName);
                    metadata.put("roles", safeRoleNames);
                    metadata.put("tenant_id", tenantId);
                })
                .build();
        try {
            authorizationService.save(authorization);
            log.info("Saved password-grant opaque token for principal={} tokenId={}", principalName, accessToken.getTokenValue().substring(0, Math.min(8, accessToken.getTokenValue().length())));
        } catch (Exception ex) {
            log.error("Failed to save authorization for principal={}", principalName, ex);
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("scope", String.join(" ", authorizedScopes));
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, null, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JuviAIPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
