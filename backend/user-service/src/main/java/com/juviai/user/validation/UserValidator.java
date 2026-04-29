package com.juviai.user.validation;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.juviai.user.domain.User;
import com.juviai.user.domain.UserStatus;
import com.juviai.user.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateSignup(String email, String mobile, String rawPassword) {
        requireEmail(email);
        requirePassword(rawPassword);
        User existing = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existing != null) {
            if (existing.getStatus() != UserStatus.PENDING_VERIFICATION) {
                throw new IllegalArgumentException("Email already in use");
            }
            // Pending verification user can re-attempt signup; allow continuing.
            // Mobile uniqueness must allow same user to reuse their own mobile.
            ensureMobileUniqueIfPresent(mobile, existing.getId());
            return;
        }

        if (mobile != null && !mobile.isBlank()) {
            String trimmed = mobile.trim();
            userRepository.findByMobile(trimmed)
                .ifPresent(u -> {
                    if (u.getStatus() != UserStatus.PENDING_VERIFICATION) {
                        throw new IllegalArgumentException("Mobile number already in use");
                    }
                });
        }
    }

    public void validateAdminCreateUser(String email, String mobile) {
        requireEmail(email);
        ensureEmailUnique(email);
        ensureMobileUniqueIfPresent(mobile, null);
    }

    public void validateAdminUpdateUser(UUID id, String mobile) {
        if (id == null) throw new IllegalArgumentException("User ID is required");
        if (mobile != null && !mobile.isBlank()) {
            String trimmed = mobile.trim();
            userRepository.findByMobile(trimmed)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(x -> { throw new IllegalArgumentException("Mobile number already in use"); });
        }
    }

    public void validateInviteEmployee(String email) {
        requireEmail(email);
        ensureEmailUnique(email);
    }

    public void validatePasswordSetup(String token, String newPassword) {
        if (Objects.isNull(token)) {
            throw new IllegalArgumentException("Token is required");
        }
        requirePassword(newPassword);
    }

    private void requireEmail(String email) {
        if (Objects.isNull(email)) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private void requirePassword(String rawPassword) {
        if (Objects.isNull(rawPassword)|| rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }

    private void ensureEmailUnique(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
    }

    private void ensureMobileUniqueIfPresent(String mobile, UUID allowUserId) {
        if (Objects.nonNull(mobile) && !mobile.isBlank()) {
            String trimmed = mobile.trim();
            userRepository.findByMobile(trimmed)
                    .filter(u -> !u.getId().equals(allowUserId))
                    .ifPresent(x -> { throw new IllegalArgumentException("Mobile number already in use"); });
        }
    }
}
