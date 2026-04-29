package com.juviai.auth.service;

import com.juviai.auth.entity.OtpEntity;
import com.juviai.auth.repository.OtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int MAX_RESENDS = 3;

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final UserServiceClient userServiceClient;
    private final StringRedisTemplate redis;

    private final SecureRandom random = new SecureRandom();
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public OtpService(OtpRepository otpRepository,
                      EmailService emailService,
                      UserServiceClient userServiceClient,
                      StringRedisTemplate redis) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.userServiceClient = userServiceClient;
        this.redis = redis;
    }

    @Transactional
    public void generateAndSend(UUID userId, String email, String tenant) {
        String otp = generateOtp();

        OtpEntity entity = new OtpEntity();
        entity.setUserId(userId);
        entity.setOtpHash(encoder.encode(otp));
        entity.setExpiryTime(Instant.now().plus(OTP_TTL));
        entity.setAttempts(0);
        entity.setVerified(false);
        entity.setCreatedAt(Instant.now());
        entity.setResendCount(0);
        otpRepository.save(entity);

        try {
            emailService.sendOtpEmail(userId, email, otp, tenant);
        } catch (Exception e) {
            // Never fail signup because of email issues
            log.warn("Async email trigger failed userId={} tenant={}", userId, tenant, e);
        }
    }

    @Transactional
    public void resend(UUID userId, String email, String tenant) {
        enforceRateLimit("resend", userId, tenant);

        OtpEntity latest = otpRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseGet(() -> {
                    OtpEntity e = new OtpEntity();
                    e.setUserId(userId);
                    e.setCreatedAt(Instant.now());
                    e.setExpiryTime(Instant.now());
                    e.setOtpHash(encoder.encode(generateOtp()));
                    e.setAttempts(0);
                    e.setVerified(false);
                    e.setResendCount(0);
                    return otpRepository.save(e);
                });

        if (latest.isVerified()) {
            throw new IllegalStateException("OTP already verified");
        }
        if (latest.getResendCount() >= MAX_RESENDS) {
            throw new IllegalStateException("Resend limit reached");
        }

        String otp = generateOtp();
        latest.setOtpHash(encoder.encode(otp));
        latest.setExpiryTime(Instant.now().plus(OTP_TTL));
        latest.setAttempts(0);
        latest.setResendCount(latest.getResendCount() + 1);
        otpRepository.save(latest);

        try {
            emailService.sendOtpEmail(userId, email, otp, tenant);
        } catch (Exception e) {
            log.warn("Async resend email trigger failed userId={} tenant={}", userId, tenant, e);
        }
    }

    @Transactional
    public boolean verify(UUID userId, String otp, String tenant) {
        enforceRateLimit("verify", userId, tenant);

        OtpEntity latest = otpRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found"));

        if (latest.isVerified()) {
            return true;
        }
        if (latest.getExpiryTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP expired");
        }
        if (latest.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
            userServiceClient.updateStatus(userId.toString(), "BLOCKED", tenant);
            throw new IllegalStateException("Too many invalid attempts");
        }

        boolean ok = encoder.matches(otp, latest.getOtpHash());
        if (!ok) {
            latest.setAttempts(latest.getAttempts() + 1);
            otpRepository.save(latest);
            if (latest.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
                userServiceClient.updateStatus(userId.toString(), "BLOCKED", tenant);
            }
            return false;
        }

        latest.setVerified(true);
        otpRepository.save(latest);
        userServiceClient.updateStatus(userId.toString(), "ACTIVE", tenant);
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<OtpEntity> getLatest(UUID userId) {
        return otpRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    // ── Phone-based OTP (for OTP-login / food delivery flow) ─────────────────

    private static String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) return null;
        String trimmed = phoneNumber.trim();
        if (trimmed.isBlank()) return trimmed;
        // Keep digits only so that "+91 83099-48791" and "8309948791" map to same key.
        return trimmed.replaceAll("[^0-9]", "");
    }

    /**
     * Generate and record an OTP associated with a phone number.
     * Used when the user may not yet exist in the system (auto-registration path).
     * Delivery is a no-op here — caller (OtpLoginService) must route to SMS gateway.
     *
     * @param phoneNumber normalised E.164 phone string
     * @param tenant      tenant context
     * @return the plaintext OTP (caller must send it via SMS)
     */
    @Transactional
    public String generateForPhone(String phoneNumber, String tenant) {
        String normalized = normalizePhone(phoneNumber);
        enforcePhoneRateLimit("send", normalized, tenant);

        String otp = generateOtp();

        OtpEntity entity = new OtpEntity();
        entity.setPhoneNumber(normalized);
        entity.setOtpHash(encoder.encode(otp));
        entity.setExpiryTime(Instant.now().plus(OTP_TTL));
        entity.setAttempts(0);
        entity.setVerified(false);
        entity.setCreatedAt(Instant.now());
        entity.setResendCount(0);
        otpRepository.save(entity);

        log.info("Phone OTP generated for phone={} tenant={}", normalized, tenant);
        return otp;
    }

    /**
     * Verify a phone-based OTP.
     *
     * @return true if OTP matches and is within TTL
     * @throws IllegalArgumentException if not found or expired
     * @throws IllegalStateException    if rate limit exceeded
     */
    @Transactional
    public boolean verifyForPhone(String phoneNumber, String otp, String tenant) {
        String normalized = normalizePhone(phoneNumber);
        enforcePhoneRateLimit("verify", normalized, tenant);

        OtpEntity latest = otpRepository.findTopByPhoneNumberOrderByCreatedAtDesc(normalized)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found for phone: " + normalized));

        if (latest.isVerified()) {
            return true;
        }
        if (latest.getExpiryTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP expired");
        }
        if (latest.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
            throw new IllegalStateException("Too many invalid OTP attempts for phone: " + normalized);
        }

        boolean ok = encoder.matches(otp, latest.getOtpHash());
        if (!ok) {
            latest.setAttempts(latest.getAttempts() + 1);
            otpRepository.save(latest);
            if (latest.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
                log.warn("Max OTP attempts reached for phone={}", normalized);
            }
            return false;
        }

        latest.setVerified(true);
        otpRepository.save(latest);
        return true;
    }

    /** Clean up all phone OTPs for the given number after successful login. */
    @Transactional
    public void deleteForPhone(String phoneNumber) {
        otpRepository.deleteByPhoneNumber(normalizePhone(phoneNumber));
    }

    private void enforcePhoneRateLimit(String action, String phoneNumber, String tenant) {
        String key = "auth:otp:phone:" + action + ":" + phoneNumber + ":" + (tenant == null ? "default" : tenant);
        try {
            Long cur = redis.opsForValue().increment(key);
            if (cur != null && cur == 1) {
                redis.expire(key, Duration.ofMinutes(10));
            }
            if (cur != null && cur > 10) {
                throw new IllegalArgumentException("Too many OTP requests for this phone number");
            }
        } catch (RedisConnectionFailureException ex) {
            // Redis down — do not block OTP flow
        }
    }

    private String generateOtp() {
        int value = 100000 + random.nextInt(900000);
        return String.valueOf(value);
    }

    private void enforceRateLimit(String action, UUID userId, String tenant) {
        // Basic placeholder: allow up to 30 calls / 10 minutes per user per action
        // Keeps structure so it can later be replaced by Redis bucket / gateway rate limiting / etc.
        String key = "auth:otp:" + action + ":" + userId + ":" + (tenant == null ? "default" : tenant);
        try {
            Long cur = redis.opsForValue().increment(key);
            if (cur != null && cur == 1) {
                redis.expire(key, Duration.ofMinutes(10));
            }
            if (cur != null && cur > 30) {
                throw new IllegalArgumentException("Too many requests");
            }
        } catch (RedisConnectionFailureException ex) {
            // Redis is used only for rate-limiting; do not fail OTP flows if Redis is down
        }
    }
}
