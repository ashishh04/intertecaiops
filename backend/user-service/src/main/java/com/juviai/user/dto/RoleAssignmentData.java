package com.juviai.user.dto;

import com.juviai.user.domain.ScopeType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire-format for a single {@link com.juviai.user.domain.RoleAssignment}.
 * Populated by {@code RoleAssignmentPopulator} and returned from the REST
 * endpoints under {@code /api/role-assignments}.
 */
@Data
public class RoleAssignmentData {

    private UUID id;

    private UUID userId;
    private String userEmail;

    private UUID roleId;
    private String roleName;

    private ScopeType scopeType;
    private UUID scopeId;

    private String assignedBy;
    private Instant expiresAt;
    private boolean active;

    /** True when {@code active} and (no expiry or expiry is in the future). */
    private boolean effective;
}
