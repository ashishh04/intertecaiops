package com.juviai.user.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Generic (polymorphic) role-assignment record. A single row means
 * "user U has role R within the scope (scopeType, scopeId)".
 *
 * <p>This replaces the per-scope junction tables — {@code BusinessUserRole}
 * and {@code ProjectUserRole} — with one uniform model that also supports
 * STORE, WAREHOUSE, DEPARTMENT, TEAM, etc. without schema changes.</p>
 *
 * <p>Indexes are tuned for the three hot queries:</p>
 * <ul>
 *   <li>{@code (user_id, active)} — login fan-out, "give me my roles"</li>
 *   <li>{@code (scope_type, scope_id, role_id)} — "who admins scope X?"</li>
 *   <li>{@code (user_id, scope_type)} — "which scopes does U have a role in?"</li>
 * </ul>
 */
@Setter
@Getter
@Entity
@Table(
    name = "role_assignments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_role_assignments_user_role_scope",
        columnNames = {"user_id", "role_id", "scope_type", "scope_id"}
    ),
    indexes = {
        @Index(name = "idx_role_assignments_user_active",
               columnList = "user_id, active"),
        @Index(name = "idx_role_assignments_scope",
               columnList = "scope_type, scope_id, role_id"),
        @Index(name = "idx_role_assignments_user_scope_type",
               columnList = "user_id, scope_type")
    }
)
public class RoleAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", length = 64, nullable = false)
    private ScopeType scopeType;

    /** Null when {@link #scopeType} is {@link ScopeType#GLOBAL}. */
    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "assigned_by", length = 255)
    private String assignedBy;

    /** Optional expiry. {@code null} means non-expiring. */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** Soft-disable flag; {@code false} hides the row from authority checks. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    public RoleAssignment() {
        // JPA
    }

    public RoleAssignment(User user, Role role, ScopeType scopeType, UUID scopeId,
                          String assignedBy) {
        this.user = user;
        this.role = role;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.assignedBy = assignedBy;
    }

    /**
     * True when this row should be honored right now: {@link #active}
     * is set and any {@link #expiresAt} is still in the future.
     */
    public boolean isEffective() {
        if (!active) return false;
        return expiresAt == null || expiresAt.isAfter(Instant.now());
    }

    public ScopeRef scopeRef() {
        return ScopeRef.of(scopeType, scopeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleAssignment that)) return false;
        return Objects.equals(user == null ? null : user.getId(),
                              that.user == null ? null : that.user.getId())
            && Objects.equals(role == null ? null : role.getId(),
                              that.role == null ? null : that.role.getId())
            && scopeType == that.scopeType
            && Objects.equals(scopeId, that.scopeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            user == null ? null : user.getId(),
            role == null ? null : role.getId(),
            scopeType,
            scopeId
        );
    }

    @Override
    public String toString() {
        return "RoleAssignment{" +
                "user=" + (user == null ? null : user.getId()) +
                ", role=" + (role == null ? null : role.getName()) +
                ", scope=" + scopeType + ":" + scopeId +
                ", active=" + active +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
