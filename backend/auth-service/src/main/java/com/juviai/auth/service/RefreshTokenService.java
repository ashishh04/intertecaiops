package com.juviai.auth.service;

import com.juviai.auth.domain.RefreshTokenSession;
import com.juviai.auth.repo.RefreshTokenSessionRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenSessionRepository repo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenSessionRepository repo) {
        this.repo = repo;
    }

    public record RefreshTokenPair(String refreshToken, RefreshTokenSession session) {}

    @Transactional
    public RefreshTokenPair createSession(UUID userId,
                                         String deviceId,
                                         String userAgent,
                                         String ipAddress,
                                         Duration ttl) {
        UUID sessionId = UUID.randomUUID();
        String raw = generateRawToken(sessionId);

        RefreshTokenSession s = new RefreshTokenSession();
        s.setId(sessionId);
        s.setUserId(userId);
        s.setDeviceId(deviceId);
        s.setUserAgent(userAgent);
        s.setIpAddress(ipAddress);
        s.setCreatedAt(Instant.now());
        s.setExpiryDate(Instant.now().plus(ttl));
        s.setRevoked(false);
        s.setHashedToken(encoder.encode(raw));

        repo.save(s);
        return new RefreshTokenPair(raw, s);
    }

    @Transactional
    public RefreshTokenPair rotate(String presentedRefreshToken,
                                  String deviceId,
                                  String userAgent,
                                  String ipAddress,
                                  Duration ttl) {
        UUID sessionId = parseSessionId(presentedRefreshToken);
        RefreshTokenSession existing = repo.findByIdAndRevokedFalse(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!existing.getDeviceId().equals(deviceId)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        if (existing.getExpiryDate().isBefore(Instant.now())) {
            existing.setRevoked(true);
            repo.save(existing);
            throw new IllegalArgumentException("Refresh token expired");
        }
        if (!encoder.matches(presentedRefreshToken, existing.getHashedToken())) {
            existing.setRevoked(true);
            repo.save(existing);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        existing.setRevoked(true);
        repo.save(existing);

        return createSession(existing.getUserId(), deviceId, userAgent, ipAddress, ttl);
    }

    @Transactional
    public void revoke(String presentedRefreshToken, String deviceId) {
        UUID sessionId = parseSessionId(presentedRefreshToken);
        repo.findByIdAndRevokedFalse(sessionId).ifPresent(s -> {
            if (s.getDeviceId().equals(deviceId)) {
                s.setRevoked(true);
                repo.save(s);
            }
        });
    }

    @Transactional
    public void revokeAll(UUID userId) {
        var sessions = repo.findAllByUserIdAndRevokedFalse(userId);
        for (RefreshTokenSession s : sessions) {
            s.setRevoked(true);
        }
        repo.saveAll(sessions);
    }

    private String generateRawToken(UUID sessionId) {
        byte[] secret = new byte[48];
        random.nextBytes(secret);
        String secretB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        return sessionId + "." + secretB64;
    }

    private UUID parseSessionId(String token) {
        try {
            String idPart = token.split("\\.", 2)[0];
            return UUID.fromString(idPart);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}
