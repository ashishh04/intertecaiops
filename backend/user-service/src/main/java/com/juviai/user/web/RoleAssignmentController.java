package com.juviai.user.web;

import com.juviai.user.domain.ScopeType;
import com.juviai.user.dto.RoleAssignmentData;
import com.juviai.user.dto.RoleAssignmentRequest;
import com.juviai.user.facade.RoleAssignmentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Generic scoped-role-assignment endpoints. Replaces the per-scope paths
 * under {@code /api/roles/business/*} and {@code /api/roles/project/*} with
 * one uniform {@code /api/role-assignments} surface that works for every
 * {@link ScopeType} (B2B_UNIT, STORE, PROJECT, WAREHOUSE, ...).
 *
 * <p>The legacy per-scope controllers continue to work during migration —
 * they delegate to the same service.</p>
 */
@RestController
@RequestMapping("/api/role-assignments")
@Validated
@RequiredArgsConstructor
public class RoleAssignmentController {

    private final RoleAssignmentFacade roleAssignmentFacade;

    /**
     * SpEL guard for mutations: caller must be a platform admin, OR hold an
     * admin-like role at the target scope (or a parent scope thanks to the
     * resolver cascade). A user can grant/revoke roles only at scopes they
     * already administer — preventing horizontal privilege escalation.
     */
    private static final String SCOPE_ADMIN_GUARD =
            "hasAnyRole('ADMIN','BUSINESS_ADMIN') "
            + "or @scopedSecurity.has('ROLE_BUSINESS_ADMIN', #request.scopeType, #request.scopeId) "
            + "or @scopedSecurity.has('ROLE_STORE_ADMIN', #request.scopeType, #request.scopeId)";

    private static final String SCOPE_ADMIN_PARAM_GUARD =
            "hasAnyRole('ADMIN','BUSINESS_ADMIN') "
            + "or @scopedSecurity.has('ROLE_BUSINESS_ADMIN', #scopeType, #scopeId) "
            + "or @scopedSecurity.has('ROLE_STORE_ADMIN', #scopeType, #scopeId)";

    /** Grant a role to a user at a scope. Caller must admin the target scope. */
    @PostMapping
    @PreAuthorize(SCOPE_ADMIN_GUARD)
    public ResponseEntity<RoleAssignmentData> assign(
            @Valid @RequestBody RoleAssignmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String assignedBy = jwt != null ? jwt.getSubject() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleAssignmentFacade.assign(request, assignedBy));
    }

    /** Hard-revoke (delete) an assignment. */
    @DeleteMapping
    @PreAuthorize(SCOPE_ADMIN_PARAM_GUARD)
    public ResponseEntity<Void> revoke(
            @RequestParam UUID userId,
            @RequestParam String roleName,
            @RequestParam ScopeType scopeType,
            @RequestParam(required = false) UUID scopeId) {
        roleAssignmentFacade.revoke(userId, roleName, scopeType, scopeId);
        return ResponseEntity.noContent().build();
    }

    /** Soft-disable an assignment (keep the row for audit). */
    @PostMapping("/deactivate")
    @PreAuthorize(SCOPE_ADMIN_PARAM_GUARD)
    public ResponseEntity<RoleAssignmentData> deactivate(
            @RequestParam UUID userId,
            @RequestParam String roleName,
            @RequestParam ScopeType scopeType,
            @RequestParam(required = false) UUID scopeId) {
        return ResponseEntity.ok(
                roleAssignmentFacade.deactivate(userId, roleName, scopeType, scopeId));
    }

    /**
     * All assignments held by a user. Read guard is more permissive than the
     * write guards: a platform admin or any user with a global BUSINESS_ADMIN
     * role can query any user's assignments. Self-lookup is delegated to the
     * more specific endpoints a user already has ({@code /api/users/me/*}).
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN') "
            + "or @scopedSecurity.hasGlobal('ROLE_BUSINESS_ADMIN')")
    public ResponseEntity<List<RoleAssignmentData>> listForUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(roleAssignmentFacade.listForUser(userId, activeOnly));
    }

    /**
     * Everyone assigned at a specific scope. Use {@code scopeId=null}
     * for GLOBAL. Reader must admin the scope (or be a platform admin).
     */
    @GetMapping("/scopes/{scopeType}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN') "
            + "or @scopedSecurity.has('ROLE_BUSINESS_ADMIN', #scopeType, #scopeId) "
            + "or @scopedSecurity.has('ROLE_STORE_ADMIN', #scopeType, #scopeId)")
    public ResponseEntity<List<RoleAssignmentData>> listAtScope(
            @PathVariable ScopeType scopeType,
            @RequestParam(required = false) UUID scopeId) {
        return ResponseEntity.ok(roleAssignmentFacade.listAtScope(scopeType, scopeId));
    }

    /**
     * Check: does this user currently have {@code roleName} at the scope?
     * Walks the scope parent chain, so a B2B-unit admin will return true
     * for child stores / projects when the resolver declares the role as
     * inheritable.
     *
     * <p>Open to any authenticated caller — the response reveals only a
     * boolean and is required by policy-aware clients.</p>
     */
    @GetMapping("/has-role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> hasRole(
            @RequestParam UUID userId,
            @RequestParam String roleName,
            @RequestParam ScopeType scopeType,
            @RequestParam(required = false) UUID scopeId) {
        boolean has = roleAssignmentFacade.hasRole(userId, roleName, scopeType, scopeId);
        return ResponseEntity.ok(Map.of("hasRole", has));
    }

    /**
     * Whitelist of roles that a user may self-grant via the internal
     * {@code /internal/self-grant} endpoint. Strictly limited to scope-admin
     * roles that are legitimately claimed by "the user who just created this
     * scoped resource" — B2B Unit, Store, Project — and never to platform-level
     * roles like {@code ROLE_ADMIN}.
     */
    private static final Set<String> SELF_GRANTABLE_ROLES = Set.of(
            "ROLE_BUSINESS_ADMIN",
            "ROLE_STORE_ADMIN",
            "ROLE_PROJECT_MANAGER"
    );

    /**
     * Scope types that the self-grant path is allowed to target. The caller
     * must be creating a brand-new scoped resource (B2B Unit, Store, Project)
     * and is claiming admin over it.
     */
    private static final Set<ScopeType> SELF_GRANTABLE_SCOPES = Set.of(
            ScopeType.B2B_UNIT,
            ScopeType.STORE,
            ScopeType.PROJECT
    );

    /**
     * Internal self-grant endpoint for the "creator becomes admin" workflow
     * (CLAUDE.md rules 2 and 3). Downstream services — commerce-service on
     * store creation, project-service on project creation — call this right
     * after persisting the new scoped resource so that the creating user
     * receives the corresponding scope-admin role.
     *
     * <p><b>Security:</b>
     * <ul>
     *   <li>Caller must be authenticated (JWT required).</li>
     *   <li>Recipient is always the caller themselves — userId is pulled from
     *       the JWT subject, never from the request body.</li>
     *   <li>{@code roleName} must be in {@link #SELF_GRANTABLE_ROLES}.</li>
     *   <li>{@code scopeType} must be in {@link #SELF_GRANTABLE_SCOPES}; a
     *       non-null {@code scopeId} is required.</li>
     * </ul>
     *
     * <p><b>Note:</b> because any authenticated user could call this with an
     * arbitrary existing scopeId, downstream services should treat the scopeId
     * as evidence that the caller just created the resource. A hardened future
     * revision should wrap this in service-to-service authentication. See
     * CLAUDE.md for context.
     */
    @PostMapping("/internal/self-grant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleAssignmentData> selfGrant(
            @RequestParam String roleName,
            @RequestParam ScopeType scopeType,
            @RequestParam UUID scopeId,
            Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!SELF_GRANTABLE_ROLES.contains(roleName)) {
            return ResponseEntity.badRequest().build();
        }
        if (!SELF_GRANTABLE_SCOPES.contains(scopeType) || scopeId == null) {
            return ResponseEntity.badRequest().build();
        }

        // GatewayAuthFilter sets auth.getName() to the userId UUID (via X-User-Id header).
        UUID callerId = UUID.fromString(auth.getName());
        RoleAssignmentRequest req = new RoleAssignmentRequest();
        req.setUserId(callerId);
        req.setRoleName(roleName);
        req.setScopeType(scopeType);
        req.setScopeId(scopeId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleAssignmentFacade.assign(req, auth.getName()));
    }

    /**
     * "Which scope ids does user U hold role R at (within scope type T)?"
     * — drives side-nav filtering in module UIs. Platform admin or global
     * BUSINESS_ADMIN only; the side-nav endpoint for the caller themselves
     * belongs to {@code /api/users/me/...}.
     */
    @GetMapping("/users/{userId}/scope-ids")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN') "
            + "or @scopedSecurity.hasGlobal('ROLE_BUSINESS_ADMIN')")
    public ResponseEntity<List<UUID>> getScopeIdsForUserRole(
            @PathVariable UUID userId,
            @RequestParam String roleName,
            @RequestParam ScopeType scopeType) {
        return ResponseEntity.ok(
                roleAssignmentFacade.getScopeIdsForUserRole(userId, roleName, scopeType));
    }
}
