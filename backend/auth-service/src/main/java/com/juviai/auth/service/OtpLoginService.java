package com.juviai.auth.service;

import com.juviai.auth.web.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates the phone-OTP login flow aligned with juvaryab2b but using
 * skillratbackend's own token infrastructure (BCrypt OTP, Redis rate-limiting,
 * OAuth2AuthorizationService, JwtTokenService, RefreshTokenService).
 *
 * <h3>Send flow (POST /auth/otp/send):</h3>
 * <ol>
 *   <li>Rate-limit the phone number (handled inside OtpService).</li>
 *   <li>Generate a 6-digit OTP, BCrypt-hash it, persist in otp_verification.</li>
 *   <li>Return the plaintext OTP — the calling layer (SMS gateway / stub) delivers it.</li>
 * </ol>
 *
 * <h3>Login flow (POST /auth/otp/login):</h3>
 * <ol>
 *   <li>Verify the presented OTP against the stored BCrypt hash.</li>
 *   <li>Look up the user by phone via user-service; auto-register if absent.</li>
 *   <li>Mint access + refresh tokens (same pair issued by AuthService.login).</li>
 *   <li>Clean up the used OTP row.</li>
 * </ol>
 */
@Service
public class OtpLoginService {

    private static final Logger log = LoggerFactory.getLogger(OtpLoginService.class);

    private final OtpService otpService;
    private final UserServiceClient userServiceClient;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository registeredClientRepository;

    private final Duration accessTtl;
    private final Duration refreshTtl;

    public OtpLoginService(OtpService otpService,
                           UserServiceClient userServiceClient,
                           JwtTokenService jwtTokenService,
                           RefreshTokenService refreshTokenService,
                           OAuth2AuthorizationService authorizationService,
                           RegisteredClientRepository registeredClientRepository,
                           @Value("${skillrat.auth.access-ttl-minutes:15}") long accessTtlMinutes,
                           @Value("${skillrat.auth.refresh-ttl-days:30}") long refreshTtlDays) {
        this.otpService = otpService;
        this.userServiceClient = userServiceClient;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.authorizationService = authorizationService;
        this.registeredClientRepository = registeredClientRepository;
        this.accessTtl = Duration.ofMinutes(accessTtlMinutes);
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    /**
     * Generate an OTP for the given phone number and return it as plaintext.
     * The caller is responsible for delivering it via an SMS gateway.
     *
     * @param phoneNumber normalised phone string
     * @param tenant      tenant context (from X-JuviAI-Tenant header)
     * @return plaintext OTP (for SMS dispatch)
     */
    public String sendOtp(String phoneNumber, String tenant) {
        return otpService.generateForPhone(phoneNumber, tenant);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Verify the OTP and, on success, issue an access + refresh token pair.
     * If no user exists for {@code phoneNumber} a new account is auto-created.
     *
     * @param phoneNumber phone number (primary contact)
     * @param otp         plaintext OTP submitted by the client
     * @param fullName    display name — used only during auto-registration
     * @param deviceId    device fingerprint for session tracking
     * @param userAgent   HTTP User-Agent
     * @param ipAddress   client IP
     * @param tenant      tenant context
     * @return access + refresh token pair
     */
    public TokenResponse loginWithOtp(String phoneNumber,
                                      String otp,
                                      String fullName,
                                      String deviceId,
                                      String userAgent,
                                      String ipAddress,
                                      String tenant) {
        // Step 1: verify OTP (throws on failure / expiry / rate-limit)
        boolean valid = otpService.verifyForPhone(phoneNumber, otp, tenant);
        if (!valid) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Step 2: find or auto-register user
        Map<?, ?> user = userServiceClient.findByPhone(phoneNumber, tenant);
        if (user == null || user.get("id") == null) {
            log.info("Phone {} not found — auto-registering new user (tenant={})", phoneNumber, tenant);
            user = userServiceClient.createFromPhone(phoneNumber, fullName, tenant);
        }
        if (user == null || user.get("id") == null) {
            throw new IllegalStateException("Failed to resolve or create user for phone: " + phoneNumber);
        }

        UUID userId = UUID.fromString(String.valueOf(user.get("id")));
        // Phone users may not have an email; fall back to phone as the principal name
        String email = user.get("email") != null ? String.valueOf(user.get("email")) : phoneNumber;
        List<String> roles = (user.get("roles") instanceof List<?> r)
                ? r.stream().map(String::valueOf).toList()
                : List.of("ROLE_USER");
        int tokenVersion = 0;
        Object tv = user.get("tokenVersion");
        if (tv != null) {
            try { tokenVersion = Integer.parseInt(String.valueOf(tv)); } catch (Exception ignored) {}
        }

        // Step 3: mint tokens (same mechanism as AuthService.login)
        JwtTokenService.AccessTokenResult access =
                jwtTokenService.mintAccessToken(userId, email, roles, tokenVersion, accessTtl);
        saveOpaqueAccessToken(access.token(), accessTtl, userId, email, roles, tenant);
        RefreshTokenService.RefreshTokenPair refresh =
                refreshTokenService.createSession(userId, deviceId, userAgent, ipAddress, refreshTtl);

        // Step 4: clean up consumed OTP
        try {
            otpService.deleteForPhone(phoneNumber);
        } catch (Exception e) {
            log.warn("Failed to clean up phone OTP for {} — non-fatal", phoneNumber, e);
        }

        return new TokenResponse(access.token(), refresh.refreshToken(), access.expiresInSeconds());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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
        Instant issuedAt  = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, tokenValue, issuedAt, expiresAt, client.getScopes());

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
}
