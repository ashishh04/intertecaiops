package com.juviai.auth.web;

import com.juviai.auth.service.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * DEV-ONLY controller — NOT included in production builds.
 * <p>
 * Allows testers to activate a newly-registered user without needing a real
 * email inbox (useful when RESEND_API_KEY is not configured locally).
 * <p>
 * Endpoints:
 * <pre>
 *   POST /dev/auth/activate/{userId}          — sets user status=ACTIVE (skips OTP check)
 *   POST /dev/auth/block/{userId}             — sets user status=BLOCKED  (test helper)
 * </pre>
 */
@Profile({"dev", "local"})
@RestController
@RequestMapping("/dev/auth")
public class DevOtpController {

    private static final Logger log = LoggerFactory.getLogger(DevOtpController.class);

    private final UserServiceClient userServiceClient;

    public DevOtpController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * Directly activates a user account (status = ACTIVE, active = true).
     * Use this in local / CI environments where email delivery is not configured.
     *
     * <pre>
     * POST /dev/auth/activate/{userId}
     * Optional header: X-JuviAI-Tenant: &lt;tenant&gt;
     * </pre>
     */
    @PostMapping("/activate/{userId}")
    public ResponseEntity<?> activate(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {

        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        log.warn("[DEV] Bypassing OTP — activating userId={} tenant={}", userId, tenant);
        userServiceClient.updateStatus(userId.toString(), "ACTIVE", tenant);
        return ResponseEntity.ok(Map.of(
                "userId", userId.toString(),
                "status", "ACTIVE",
                "active", true,
                "note", "DEV bypass — not available in production"
        ));
    }

    /**
     * Blocks a user (status = BLOCKED, active = false). Useful for testing
     * the account-locked flow without triggering real lockout conditions.
     */
    @PostMapping("/block/{userId}")
    public ResponseEntity<?> block(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-JuviAI-Tenant", required = false) String tenantHeader) {

        String tenant = StringUtils.hasText(tenantHeader) ? tenantHeader : "default";
        log.warn("[DEV] Blocking userId={} tenant={}", userId, tenant);
        userServiceClient.updateStatus(userId.toString(), "BLOCKED", tenant);
        return ResponseEntity.ok(Map.of(
                "userId", userId.toString(),
                "status", "BLOCKED",
                "active", false,
                "note", "DEV bypass — not available in production"
        ));
    }
}
