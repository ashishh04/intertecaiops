package com.juviai.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juviai.auth.web.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UserServiceClient userClient;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository registeredClientRepository;
    private final StringRedisTemplate redis;
    private final OtpService otpService;

    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final int maxLoginAttempts;
    private final Duration loginAttemptWindow;

    public AuthService(UserServiceClient userClient,
                       JwtTokenService jwtTokenService,
                       RefreshTokenService refreshTokenService,
                       OAuth2AuthorizationService authorizationService,
                       RegisteredClientRepository registeredClientRepository,
                        StringRedisTemplate redis,
                        OtpService otpService,
                       @Value("${skillrat.auth.access-ttl-minutes:15}") long accessTtlMinutes,
                       @Value("${skillrat.auth.refresh-ttl-days:30}") long refreshTtlDays,
                       @Value("${skillrat.auth.rate-limit.max-attempts:10}") int maxLoginAttempts,
                       @Value("${skillrat.auth.rate-limit.window-seconds:600}") long windowSeconds) {
        this.userClient = userClient;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.authorizationService = authorizationService;
        this.registeredClientRepository = registeredClientRepository;
        this.redis = redis;
        this.otpService = otpService;
        this.accessTtl = Duration.ofMinutes(accessTtlMinutes);
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
        this.maxLoginAttempts = maxLoginAttempts;
        this.loginAttemptWindow = Duration.ofSeconds(windowSeconds);
    }

    public TokenResponse login(String username,
                              String password,
                              String deviceId,
                              String userAgent,
                              String ipAddress,
                              String tenant) {
        enforceRateLimit(username, ipAddress);

        Map<?, ?> user;
        try {
            user = userClient.login(username, password, tenant);
        } catch (HttpClientErrorException e) {
            // Translate upstream errors into meaningful client-facing messages
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST
                    || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Extract the 'detail' field from the upstream problem-detail response if present
                String detail = extractDetail(e.getResponseBodyAsString());
                throw new IllegalArgumentException(detail != null ? detail : "Invalid credentials");
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new IllegalArgumentException(
                        "Account is not yet active. Please verify your email first.");
            }
            // Any other 4xx/5xx from user-service — log and surface a safe message
            log.error("User-service login call failed status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("Login failed. Please try again later.");
        }

        if (user == null || user.get("id") == null || user.get("email") == null) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        UUID userId = UUID.fromString(String.valueOf(user.get("id")));
        String email = String.valueOf(user.get("email"));
        List<String> roles = (user.get("roles") instanceof List<?> r)
                ? r.stream().map(String::valueOf).toList()
                : List.of();
        int tokenVersion = 0;
        Object tv = user.get("tokenVersion");
        if (tv != null) {
            try { tokenVersion = Integer.parseInt(String.valueOf(tv)); } catch (Exception ignored) {}
        }

        JwtTokenService.AccessTokenResult access = jwtTokenService.mintAccessToken(userId, email, roles, tokenVersion, accessTtl);
        saveOpaqueAccessToken(access.token(), accessTtl, userId, email, roles, tenant);
        RefreshTokenService.RefreshTokenPair refresh = refreshTokenService.createSession(userId, deviceId, userAgent, ipAddress, refreshTtl);

        return new TokenResponse(access.token(), refresh.refreshToken(), access.expiresInSeconds());
    }

    public TokenResponse refresh(String presentedRefreshToken,
                                String deviceId,
                                String userAgent,
                                String ipAddress,
                                String tenant) {
        RefreshTokenService.RefreshTokenPair rotated = refreshTokenService.rotate(presentedRefreshToken, deviceId, userAgent, ipAddress, refreshTtl);

        UUID userId = rotated.session().getUserId();

        Map<?, ?> authInfo = userClient.authInfo(userId.toString(), tenant);
        if (authInfo == null || authInfo.get("email") == null) {
            throw new IllegalArgumentException("User not found");
        }
        Object activeObj = authInfo.get("active");
        if (activeObj instanceof Boolean b && !b) {
            throw new IllegalArgumentException("User is inactive");
        }

        String email = String.valueOf(authInfo.get("email"));
        List<String> roles = (authInfo.get("roles") instanceof List<?> r)
                ? r.stream().map(String::valueOf).toList()
                : List.of();
        int tokenVersion = 0;
        Object tv = authInfo.get("tokenVersion");
        if (tv != null) {
            try { tokenVersion = Integer.parseInt(String.valueOf(tv)); } catch (Exception ignored) {}
        }

        JwtTokenService.AccessTokenResult access = jwtTokenService.mintAccessToken(userId, email, roles, tokenVersion, accessTtl);
        saveOpaqueAccessToken(access.token(), accessTtl, userId, email, roles, tenant);
        return new TokenResponse(access.token(), rotated.refreshToken(), access.expiresInSeconds());
    }

    private void saveOpaqueAccessToken(String tokenValue,
                                      Duration ttl,
                                      UUID userId,
                                      String email,
                                      List<String> roles,
                                      String tenant) {
        RegisteredClient client = registeredClientRepository.findByClientId("gateway");
        if (client == null) {
            throw new IllegalStateException("Missing registered client 'gateway'");
        }
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, issuedAt, expiresAt, client.getScopes());

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(client)
                .principalName(email)
                .authorizationGrantType(new AuthorizationGrantType("skillrat-direct"))
                .authorizedScopes(client.getScopes())
                .attribute(OAuth2ParameterNames.USERNAME, email)
                .token(accessToken, metadata -> {
                    metadata.put("userId", userId.toString());
                    metadata.put("username", email);
                    metadata.put("email", email);
                    metadata.put("roles", roles != null ? new ArrayList<>(roles) : new ArrayList<>());
                    metadata.put("tenant_id", tenant);
                })
                .build();
        authorizationService.save(authorization);
    }

    public void logout(String presentedRefreshToken, String deviceId) {
        refreshTokenService.revoke(presentedRefreshToken, deviceId);
    }

    public void logoutAll(UUID userId, String tenant) {
        refreshTokenService.revokeAll(userId);
        userClient.incrementTokenVersion(userId.toString(), tenant);
    }

    public Map<?, ?> register(Map<String, Object> body, String tenant) {
        if (body != null) {
            body.putIfAbsent("status", "PENDING_VERIFICATION");
            body.put("active", false);
        }
        Map<?, ?> created = userClient.signup(body, tenant);
        try {
            if (created != null && created.get("id") != null && created.get("email") != null) {
                UUID userId = UUID.fromString(String.valueOf(created.get("id")));
                String email = String.valueOf(created.get("email"));
                otpService.generateAndSend(userId, email, tenant);
            }
        } catch (Exception e) {
            // Never fail registration due to OTP/email problems
        }
        return created;
    }

    /**
     * Extracts the {@code detail} field from a RFC 7807 problem-detail JSON body.
     * Returns null if the body cannot be parsed or the field is absent.
     */
    private static String extractDetail(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return null;
        try {
            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode detail = root.path("detail");
            if (!detail.isMissingNode() && detail.isTextual()) {
                return detail.asText();
            }
            // Fallback: try "message" or "error"
            for (String key : new String[]{"message", "error"}) {
                JsonNode node = root.path(key);
                if (!node.isMissingNode() && node.isTextual()) return node.asText();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void enforceRateLimit(String username, String ipAddress) {
        String key = "auth:login:" + (username == null ? "" : username.toLowerCase()) + ":" + (ipAddress == null ? "" : ipAddress);
        try {
            Long cur = redis.opsForValue().increment(key);
            if (cur != null && cur == 1) {
                redis.expire(key, loginAttemptWindow);
            }
            if (cur != null && cur > maxLoginAttempts) {
                throw new IllegalArgumentException("Too many login attempts");
            }
        } catch (RedisConnectionFailureException ex) {
            // Redis is used only for rate-limiting; do not fail auth flows if Redis is down
        }
    }
}
