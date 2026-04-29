package com.juviai.user.service;

import com.juviai.common.exception.ResourceNotFoundException;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.RoleAssignment;
import com.juviai.user.domain.ScopeRef;
import com.juviai.user.domain.ScopeType;
import com.juviai.user.domain.User;
import com.juviai.user.repo.RoleAssignmentRepository;
import com.juviai.user.repo.RoleRepository;
import com.juviai.user.repo.UserRepository;
import com.juviai.user.security.scope.ScopeResolver;
import com.juviai.user.security.scope.ScopeResolverRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core service for the generic scoped-role model. Replaces the per-scope
 * services ({@link BusinessRoleService}, {@link ProjectRoleService}, ...)
 * with one uniform API that works for every {@link ScopeType}.
 *
 * <p>Validation delegates to the {@link ScopeResolverRegistry}: an assignment
 * must reference a role whose {@link Role#getApplicableScopes() applicableScopes}
 * includes the requested scope type, and the target scope entity must exist
 * (per the resolver for that type). If no resolver is registered for a type,
 * existence is not enforced — callers are trusted.</p>
 */
@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(RoleAssignmentService.class);

    private final RoleAssignmentRepository roleAssignmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ScopeResolverRegistry scopeResolverRegistry;

    // ------------------------------------------------------------------
    // Mutations
    // ------------------------------------------------------------------

    /**
     * Grant {@code roleName} to {@code userId} at the given scope. Idempotent:
     * if the same (user, role, scope) row already exists it is returned as-is.
     */
    @Transactional
    public RoleAssignment assign(@NonNull UUID userId,
                                 @NonNull String roleName,
                                 @NonNull ScopeType scopeType,
                                 UUID scopeId,
                                 String assignedBy) {
        return assign(userId, roleName, scopeType, scopeId, assignedBy, null);
    }

    /**
     * Grant with an optional expiry.
     */
    @Transactional
    public RoleAssignment assign(@NonNull UUID userId,
                                 @NonNull String roleName,
                                 @NonNull ScopeType scopeType,
                                 UUID scopeId,
                                 String assignedBy,
                                 Instant expiresAt) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: " + roleName));

        validateScope(role, scopeType, scopeId);

        Optional<RoleAssignment> existing = roleAssignmentRepository.findOne(
                user.getId(), role.getId(), scopeType, scopeId);

        if (existing.isPresent()) {
            RoleAssignment ra = existing.get();
            // Re-activate a soft-deleted row and refresh expiry / assigner.
            boolean changed = false;
            if (!ra.isActive()) {
                ra.setActive(true);
                changed = true;
            }
            if (expiresAt != null && !expiresAt.equals(ra.getExpiresAt())) {
                ra.setExpiresAt(expiresAt);
                changed = true;
            }
            if (assignedBy != null && !assignedBy.equals(ra.getAssignedBy())) {
                ra.setAssignedBy(assignedBy);
                changed = true;
            }
            return changed ? roleAssignmentRepository.save(ra) : ra;
        }

        RoleAssignment ra = new RoleAssignment(user, role, scopeType, scopeId, assignedBy);
        ra.setExpiresAt(expiresAt);
        return roleAssignmentRepository.save(ra);
    }

    /**
     * Revoke a role. Hard-deletes the row; callers wanting audit retention
     * should use {@link #deactivate} instead.
     */
    @Transactional
    public void revoke(@NonNull UUID userId,
                       @NonNull String roleName,
                       @NonNull ScopeType scopeType,
                       UUID scopeId) {

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: " + roleName));

        int removed = roleAssignmentRepository.deleteByUserRoleAndScope(
                userId, role.getId(), scopeType, scopeId);

        if (removed == 0) {
            log.debug("No RoleAssignment row to revoke for user={}, role={}, scope={}:{}",
                    userId, roleName, scopeType, scopeId);
        }
    }

    /** Soft-deactivate a single assignment (row kept, {@code active = false}). */
    @Transactional
    public RoleAssignment deactivate(@NonNull UUID userId,
                                     @NonNull String roleName,
                                     @NonNull ScopeType scopeType,
                                     UUID scopeId) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: " + roleName));

        RoleAssignment ra = roleAssignmentRepository
                .findOne(userId, role.getId(), scopeType, scopeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No role assignment for user " + userId
                                + " role " + roleName + " at " + scopeType + ":" + scopeId));

        ra.setActive(false);
        return roleAssignmentRepository.save(ra);
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    /** Every active (non-expired) assignment held by a user — login fan-out. */
    @Transactional(readOnly = true)
    public List<RoleAssignment> getEffectiveAssignmentsForUser(@NonNull UUID userId) {
        return roleAssignmentRepository.findActiveForUser(userId).stream()
                .filter(RoleAssignment::isEffective)
                .toList();
    }

    /** Every row for a user — admin tooling. */
    @Transactional(readOnly = true)
    public List<RoleAssignment> getAllAssignmentsForUser(@NonNull UUID userId) {
        return roleAssignmentRepository.findByUserId(userId);
    }

    /** All active assignments at a specific scope (id + type). */
    @Transactional(readOnly = true)
    public List<RoleAssignment> getAssignmentsAtScope(@NonNull ScopeType scopeType, UUID scopeId) {
        return roleAssignmentRepository.findActiveAtScope(scopeType, scopeId).stream()
                .filter(RoleAssignment::isEffective)
                .toList();
    }

    /**
     * Direct "does this user have this role at this scope right now?" check.
     * Does not consider cascading from parent scopes — use
     * {@link #hasEffectiveRole} for that.
     */
    @Transactional(readOnly = true)
    public boolean hasDirectRole(@NonNull UUID userId,
                                 @NonNull String roleName,
                                 @NonNull ScopeType scopeType,
                                 UUID scopeId) {
        return roleAssignmentRepository.existsActiveByUserAndRoleNameAndScope(
                userId, roleName, scopeType, scopeId);
    }

    /**
     * Full check including cascade: direct grant wins; otherwise we walk up
     * the scope chain via {@link ScopeResolver#parentOf} and consult each
     * ancestor's {@link ScopeResolver#inheritableRoles}.
     *
     * <p>Example: a {@code ROLE_BUSINESS_ADMIN} at the parent B2B unit grants
     * admin over a child store without requiring a separate STORE-scoped
     * assignment.</p>
     */
    @Transactional(readOnly = true)
    public boolean hasEffectiveRole(@NonNull UUID userId,
                                    @NonNull String roleName,
                                    @NonNull ScopeType scopeType,
                                    UUID scopeId) {
        if (hasDirectRole(userId, roleName, scopeType, scopeId)) {
            return true;
        }
        ScopeRef cursor = ScopeRef.of(scopeType, scopeId);
        // cap the walk to avoid cycles in misconfigured resolvers
        for (int depth = 0; depth < 8; depth++) {
            Optional<ScopeResolver> resolverOpt = scopeResolverRegistry.find(cursor.type());
            if (resolverOpt.isEmpty() || cursor.id() == null) {
                return false;
            }
            Optional<ScopeRef> parent = resolverOpt.get().parentOf(cursor.id());
            if (parent.isEmpty()) {
                return false;
            }
            cursor = parent.get();
            ScopeResolver parentResolver = scopeResolverRegistry.find(cursor.type()).orElse(null);
            if (parentResolver == null) {
                return false;
            }
            if (parentResolver.inheritableRoles().contains(roleName)
                    && hasAnyActiveRoleAt(userId, parentResolver.inheritableRoles(),
                                          cursor.type(), cursor.id())) {
                return true;
            }
        }
        return false;
    }

    /** Convenience: true when the user has any of {@code roleNames} at the scope. */
    @Transactional(readOnly = true)
    public boolean hasAnyActiveRoleAt(@NonNull UUID userId,
                                      @NonNull Collection<String> roleNames,
                                      @NonNull ScopeType scopeType,
                                      UUID scopeId) {
        for (String name : roleNames) {
            if (roleAssignmentRepository
                    .existsActiveByUserAndRoleNameAndScope(userId, name, scopeType, scopeId)) {
                return true;
            }
        }
        return false;
    }

    /** All scope ids (of the given type) where user holds the named role. */
    @Transactional(readOnly = true)
    public List<UUID> getScopeIdsForUserRole(@NonNull UUID userId,
                                             @NonNull String roleName,
                                             @NonNull ScopeType scopeType) {
        return roleAssignmentRepository
                .findScopeIdsForUserRoleAndType(userId, roleName, scopeType);
    }

    /** Roster for "who owns / admins this scope?" screens. */
    @Transactional(readOnly = true)
    public List<RoleAssignment> findAssignmentsByRoleAtScope(@NonNull String roleName,
                                                             @NonNull ScopeType scopeType,
                                                             UUID scopeId) {
        return roleAssignmentRepository
                .findByRoleNameAtScope(roleName, scopeType, scopeId).stream()
                .filter(RoleAssignment::isEffective)
                .toList();
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private void validateScope(Role role, ScopeType scopeType, UUID scopeId) {
        if (role.getApplicableScopes() != null
                && !role.getApplicableScopes().isEmpty()
                && !role.getApplicableScopes().contains(scopeType)) {
            throw new IllegalArgumentException(
                    "Role " + role.getName() + " is not applicable at scope " + scopeType
                            + " (allowed: " + role.getApplicableScopes() + ")");
        }

        Optional<ScopeResolver> resolver = scopeResolverRegistry.find(scopeType);
        if (resolver.isPresent() && !resolver.get().exists(scopeId)) {
            throw new ResourceNotFoundException(
                    "Scope " + scopeType + ":" + scopeId + " does not exist");
        }
    }
}
