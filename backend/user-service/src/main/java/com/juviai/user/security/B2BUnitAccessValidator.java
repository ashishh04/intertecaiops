package com.juviai.user.security;

import com.juviai.user.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class B2BUnitAccessValidator {

    private final UserService userService;

    public B2BUnitAccessValidator(UserService userService) {
        this.userService = userService;
    }

    public void validateCurrentUserBelongsTo(UUID b2bUnitId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }

        // GatewayAuthFilter sets auth.getName() to the userId UUID (from X-User-Id header).
        UUID userId;
        try {
            userId = UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid user identity in security context");
        }

        userService.getById(userId)
                .ifPresentOrElse(u -> {
                    UUID userB2b = (u.getB2bUnit() != null) ? u.getB2bUnit().getId() : null;
                    if (userB2b == null || !userB2b.equals(b2bUnitId)) {
                        throw new AccessDeniedException("Forbidden for this business unit");
                    }
                }, () -> {
                    throw new AccessDeniedException("User context not found");
                });
    }
}
