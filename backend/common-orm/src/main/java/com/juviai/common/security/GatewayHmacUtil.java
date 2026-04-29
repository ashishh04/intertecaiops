package com.juviai.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

/**
 * Stateless HMAC-SHA256 utility shared by the gateway (signing) and
 * every downstream service (verification).
 *
 * <h2>Signed payload format</h2>
 * <pre>
 *   userId + "|" + roles + "|" + path + "|" + epochSeconds
 * </pre>
 * All pipe-separated fields are URL-encoded so a {@code |} inside a value
 * cannot break parsing.
 *
 * <h2>Headers produced by the gateway</h2>
 * <ul>
 *   <li>{@value #HEADER_SIGNATURE}   — Base64-encoded HMAC-SHA256 of the payload</li>
 *   <li>{@value #HEADER_TIMESTAMP}   — Unix epoch seconds at signing time</li>
 *   <li>{@value #HEADER_USER_ID}     — authenticated user UUID (or empty for anonymous)</li>
 *   <li>{@value #HEADER_ROLES}       — comma-separated role names (ROLE_ prefix included)</li>
 *   <li>{@value #HEADER_USERNAME}    — user email / login name (informational only)</li>
 *   <li>{@value #HEADER_TENANT_ID}   — resolved tenant ID from TenantContext</li>
 * </ul>
 */
public final class GatewayHmacUtil {

    public static final String HEADER_SIGNATURE  = "X-Gateway-Signature";
    public static final String HEADER_TIMESTAMP  = "X-Gateway-Timestamp";
    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_ROLES      = "X-Roles";
    public static final String HEADER_USERNAME   = "X-Username";
    public static final String HEADER_TENANT_ID  = "X-Tenant-Id";

    private static final String HMAC_ALG = "HmacSHA256";

    private GatewayHmacUtil() {}

    // ── Signing (gateway side) ────────────────────────────────────────────────

    /**
     * Compute the HMAC-SHA256 signature for a request being forwarded by the gateway.
     *
     * @param secret   shared secret (same value on gateway and services)
     * @param userId   authenticated user ID or empty string for anonymous
     * @param roles    comma-separated role names
     * @param path     request URI path (no query string)
     * @param epochSec current epoch-seconds (for replay prevention)
     * @return Base64-encoded signature string
     */
    public static String sign(String secret, String userId, String roles, String path, long epochSec) {
        String payload = buildPayload(userId, roles, path, epochSec);
        return hmacBase64(secret, payload);
    }

    // ── Verification (service side) ───────────────────────────────────────────

    /**
     * Verify that the signature produced by the gateway is authentic and fresh.
     *
     * @param secret              shared secret
     * @param receivedSignature   value of {@value #HEADER_SIGNATURE}
     * @param userId              value of {@value #HEADER_USER_ID} (may be empty)
     * @param roles               value of {@value #HEADER_ROLES}   (may be empty)
     * @param path                URI path of the incoming request
     * @param timestampSec        value of {@value #HEADER_TIMESTAMP} as long
     * @param toleranceSeconds    max allowed clock skew in seconds
     * @return true if signature is valid and timestamp is within tolerance
     */
    public static boolean verify(
            String secret,
            String receivedSignature,
            String userId,
            String roles,
            String path,
            long timestampSec,
            long toleranceSeconds) {

        if (secret == null || secret.isBlank()) return false;
        if (receivedSignature == null || receivedSignature.isBlank()) return false;

        // 1. Timestamp freshness check
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestampSec) > toleranceSeconds) {
            return false; // request is stale / future-dated — likely a replay
        }

        // 2. Constant-time HMAC comparison to prevent timing attacks
        String expected = sign(secret,
                userId  == null ? "" : userId,
                roles   == null ? "" : roles,
                path    == null ? "" : path,
                timestampSec);

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8));
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static String buildPayload(String userId, String roles, String path, long epochSec) {
        return safe(userId) + "|" + safe(roles) + "|" + safe(path) + "|" + epochSec;
    }

    /** Replace pipe characters inside field values so they can't break payload parsing. */
    private static String safe(String value) {
        return value == null ? "" : value.replace("|", "%7C");
    }

    private static String hmacBase64(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG);
            mac.init(keySpec);
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }
}
