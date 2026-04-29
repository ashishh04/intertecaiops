package com.juviai.auth.web;

import com.juviai.auth.service.OtpLoginService;
import com.juviai.auth.web.dto.OtpLoginRequest;
import com.juviai.auth.web.dto.OtpSendRequest;
import com.juviai.auth.web.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Phone-based OTP authentication endpoints — aligned with juvaryab2b's UX flow
 * but using skillratbackend's infrastructure (BCrypt, Redis, OAuth2, JWT).
 *
 * <pre>
 * POST /auth/otp/send   → generate & dispatch OTP to phone
 * POST /auth/otp/login  → verify OTP, auto-register if needed, return tokens
 * </pre>
 *
 * Both endpoints are public (no Bearer token required).
 * See {@code WebSecurityConfig} for the permit-all rule on {@code /auth/**}.
 */
@RestController
@RequestMapping("/auth/otp")
public class OtpLoginController {

    private final OtpLoginService otpLoginService;

    public OtpLoginController(OtpLoginService otpLoginService) {
        this.otpLoginService = otpLoginService;
    }

    /**
     * Step 1 — request an OTP for a phone number.
     *
     * <p>The server generates a 6-digit OTP, stores a BCrypt hash, and returns
     * the plaintext OTP in the response body (dev/staging only). In production
     * the caller routes it through an SMS gateway before responding to the client.
     *
     * @param req    body containing {@code primaryContact}
     * @param tenantHeader tenant context from header (defaults to "default")
     * @return 200 OK with {@code {"status":"otp_sent"}} — or the OTP in dev mode
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(
            @RequestBody @Valid OtpSendRequest req,
            @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {

        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        String otp = otpLoginService.sendOtp(req.primaryContact, tenant);

        // NOTE: In a production deployment, hand `otp` to your SMS provider here
        // and return only {"status":"otp_sent"} to the client.
        // For development / internal testing the plaintext OTP is included.
        return ResponseEntity.ok(Map.of(
                "status", "otp_sent",
                "otp", otp          // remove in production
        ));
    }

    /**
     * Step 2 — verify OTP and obtain an access + refresh token pair.
     *
     * <p>If no account exists for the supplied phone number a new user is
     * auto-registered (using {@code fullName} when provided), matching the
     * implicit-registration pattern from juvaryab2b's {@code AuthController}.
     *
     * @param req           body containing primaryContact, otp, optional fullName + deviceId
     * @param servletRequest HTTP request for IP resolution
     * @param tenantHeader  tenant context
     * @param userAgent     client user-agent
     * @return {@link TokenResponse} with accessToken, refreshToken, expiresIn
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginWithOtp(
            @RequestBody @Valid OtpLoginRequest req,
            HttpServletRequest servletRequest,
            @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        String ip = resolveIp(servletRequest);
        String ua = userAgent != null ? userAgent : "";

        TokenResponse tokens = otpLoginService.loginWithOtp(
                req.primaryContact,
                req.otp,
                req.fullName,
                req.deviceId,
                ua,
                ip,
                tenant);

        return ResponseEntity.ok(tokens);
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
