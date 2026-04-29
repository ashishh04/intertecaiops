package com.juviai.common.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Shared security properties consumed by every downstream service.
 *
 * <p>Configure via application.yml (or env-var overrides):
 * <pre>
 * juviai:
 *   gateway:
 *     shared-secret: "change-me-at-least-256-bits-long-random-value"
 *     timestamp-tolerance-seconds: 30
 *     enforce-signature: true
 * </pre>
 *
 * <p>The {@code shared-secret} MUST be the same value across the gateway and
 * all downstream services.  Store it in Vault / Kubernetes Secret in production;
 * never commit the real value to source control.
 */
@Component
@ConfigurationProperties(prefix = "juviai.gateway")
public class GatewaySecurityProperties {

    private static final Logger log = LoggerFactory.getLogger(GatewaySecurityProperties.class);

    /** Sentinel value baked into source control — must never be used in production. */
    private static final String INSECURE_DEFAULT = "default-insecure-secret-replace-in-prod";

    /** Minimum acceptable secret length (32 chars ≈ 256 bits). */
    private static final int MIN_SECRET_LENGTH = 32;

    /**
     * HMAC-SHA256 shared secret used to sign/verify gateway-forwarded requests.
     * Must be at least 32 characters; ideally a cryptographically random 64-char hex string.
     * Set via environment variable JUVIAI_GATEWAY_SHARED_SECRET or Kubernetes Secret.
     */
    private String sharedSecret = INSECURE_DEFAULT;

    /**
     * Acceptable clock-skew window (seconds) when validating the X-Gateway-Timestamp header.
     * Requests with a timestamp outside this window are rejected as potential replays.
     * Default: 30 seconds.
     */
    private long timestampToleranceSeconds = 30;

    /**
     * When true, any request that arrives WITHOUT a valid gateway signature is rejected
     * with 401.  Set to false during development / migration to allow direct service
     * access with a plain Bearer token.
     * Default: true (strict).
     */
    private boolean enforceSignature = true;

    /**
     * Validates security properties on startup. Prevents the service from booting
     * with an insecure or missing HMAC secret when signature enforcement is enabled.
     */
    @PostConstruct
    public void validate() {
        if (enforceSignature) {
            if (sharedSecret == null || sharedSecret.isBlank()) {
                throw new IllegalStateException(
                        "[SECURITY] juviai.gateway.shared-secret is not configured. " +
                        "Set it via environment variable JUVIAI_GATEWAY_SHARED_SECRET. " +
                        "The service will not start without a valid secret when enforce-signature=true.");
            }
            if (INSECURE_DEFAULT.equals(sharedSecret)) {
                throw new IllegalStateException(
                        "[SECURITY] juviai.gateway.shared-secret is still set to the default insecure value. " +
                        "Generate a strong random secret (e.g. openssl rand -hex 32) and set it via " +
                        "environment variable JUVIAI_GATEWAY_SHARED_SECRET before deploying.");
            }
            if (sharedSecret.length() < MIN_SECRET_LENGTH) {
                throw new IllegalStateException(
                        "[SECURITY] juviai.gateway.shared-secret is too short (" + sharedSecret.length() +
                        " chars). Minimum required length is " + MIN_SECRET_LENGTH + " characters.");
            }
        } else {
            log.warn("[SECURITY] Gateway signature enforcement is DISABLED (enforce-signature=false). " +
                     "This is only safe for local development. DO NOT use in production.");
            if (INSECURE_DEFAULT.equals(sharedSecret)) {
                log.warn("[SECURITY] Default insecure shared secret is in use. " +
                         "Ensure enforce-signature=true and a strong secret are set before deploying.");
            }
        }
    }

    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String sharedSecret) { this.sharedSecret = sharedSecret; }

    public long getTimestampToleranceSeconds() { return timestampToleranceSeconds; }
    public void setTimestampToleranceSeconds(long v) { this.timestampToleranceSeconds = v; }

    public boolean isEnforceSignature() { return enforceSignature; }
    public void setEnforceSignature(boolean enforceSignature) { this.enforceSignature = enforceSignature; }
}
