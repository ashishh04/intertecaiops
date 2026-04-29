package com.juviai.auth.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final SecureRandom secureRandom = new SecureRandom();

    public JwtTokenService() {}

    public record AccessTokenResult(String token, long expiresInSeconds) {}

    public AccessTokenResult mintAccessToken(UUID userId,
                                            String email,
                                            List<String> roles,
                                            int tokenVersion,
                                            Duration ttl) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        String tokenValue = generateTokenValue();
        return new AccessTokenResult(tokenValue, Duration.between(now, exp).getSeconds());
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
