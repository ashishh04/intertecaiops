package com.juviai.user.dto;

import com.juviai.user.domain.ScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Request body for {@code POST /api/role-assignments}. {@code scopeId} is
 * {@code null} for {@link ScopeType#GLOBAL} assignments and required for all
 * others.
 */
@Data
public class RoleAssignmentRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String roleName;

    @NotNull
    private ScopeType scopeType;

    private UUID scopeId;

    /** Optional — expires_at. Null means permanent. */
    private Instant expiresAt;
}
