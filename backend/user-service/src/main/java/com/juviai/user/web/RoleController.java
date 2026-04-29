package com.juviai.user.web;

import com.juviai.user.domain.RoleModule;
import com.juviai.user.dto.RoleData;
import com.juviai.user.facade.RoleFacade;
import com.juviai.user.security.RequiresBusinessOrHrAdmin;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing roles and role assignments.
 */
@RestController
@RequestMapping("/api/roles")
@Validated
@RequiredArgsConstructor
public class RoleController {

    private final RoleFacade roleFacade;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleData> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleFacade.createRole(request.getName(), request.getDescription(), request.getB2bUnitId()));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleData> getRoleById(@PathVariable @NonNull UUID roleId) {
        return ResponseEntity.ok(roleFacade.getRoleById(roleId));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleData> updateRole(
            @PathVariable @NonNull UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(roleFacade.updateRole(roleId, request.getName(), request.getDescription()));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteRole(@PathVariable @NonNull UUID roleId) {
        roleFacade.deleteRole(roleId);
        return ResponseEntity.<Void>noContent().build();
    }

    @GetMapping("/system/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleData>> listSystemRoles(@PathVariable UUID b2bUnitId) {
        return ResponseEntity.ok(roleFacade.listSystemRoles(b2bUnitId));
    }

    @GetMapping("/business/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleData>> listBusinessRoles(@PathVariable UUID b2bUnitId) {
        return ResponseEntity.ok(roleFacade.listBusinessRoles(b2bUnitId));
    }

    /**
     * List all roles applicable to a given module (e.g. HRMS, ECOMMERCE,
     * PROJECT_MANAGEMENT). Used by module-specific admin UIs so they
     * only show the roles that are valid in their context when assigning
     * roles to a user.
     *
     * <p>When the optional {@code b2bUnitId} query parameter is provided
     * the result is restricted to system-wide roles plus roles scoped to
     * that business unit. Omit it to get every role tagged with the
     * module across the tenant.</p>
     */
    @GetMapping("/by-module/{module}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleData>> listRolesByModule(
            @PathVariable RoleModule module,
            @RequestParam(value = "b2bUnitId", required = false) UUID b2bUnitId) {
        List<RoleData> roles = (b2bUnitId == null)
                ? roleFacade.listRolesByModule(module)
                : roleFacade.listRolesByModule(module, b2bUnitId);
        return ResponseEntity.ok(roles);
    }

    // ========== Business Role Assignments ==========

    @PostMapping("/business/assign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> assignBusinessRole(
            @Valid @RequestBody AssignBusinessRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        roleFacade.assignBusinessRole(request.getUserId(), request.getBusinessId(), request.getRoleName(), jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).<Void>build();
    }

    @PostMapping("/business/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBusinessRole(@Valid @RequestBody RemoveBusinessRoleRequest request) {
        roleFacade.removeBusinessRole(request.getUserId(), request.getBusinessId(), request.getRoleName());
        return ResponseEntity.<Void>noContent().build();
    }

    @GetMapping("/business/{businessId}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleData>> getBusinessRoleAssignments(@PathVariable UUID businessId) {
        // Returns roles assigned in this business (across all users is not scoped - return empty for now)
        return ResponseEntity.ok(roleFacade.listBusinessRoles(businessId));
    }

    // ========== Project Role Assignments ==========

    @PostMapping("/project/assign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> assignProjectRole(
            @Valid @RequestBody AssignProjectRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        roleFacade.assignProjectRole(request.getUserId(), request.getProjectId(), request.getRoleName(), jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).<Void>build();
    }

    @PostMapping("/project/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeProjectRole(@Valid @RequestBody RemoveProjectRoleRequest request) {
        roleFacade.removeProjectRole(request.getUserId(), request.getProjectId(), request.getRoleName());
        return ResponseEntity.<Void>noContent().build();
    }

    @GetMapping("/project/{projectId}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleData>> getProjectRoleAssignments(@PathVariable UUID projectId) {
        // TODO: Check if ProjectRoleService.getRoleAssignmentsForProject returns role entities
        // For now, returning empty list as getUserRolesInProject requires userId
        return ResponseEntity.ok(roleFacade.getUserRolesInProject(null, projectId));
    }

    // ========== Inner Request/Response DTOs ==========

    public static class CreateRoleRequest {
        @NotBlank private String name;
        private String description;
        private UUID b2bUnitId;
        public String getName() { return name; }
        public String getDescription() { return description; }
        public UUID getB2bUnitId() { return b2bUnitId; }
        public void setName(String name) { this.name = name; }
        public void setDescription(String description) { this.description = description; }
        public void setB2bUnitId(UUID b2bUnitId) { this.b2bUnitId = b2bUnitId; }
    }

    public static class UpdateRoleRequest {
        @NotBlank private String name;
        private String description;
        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    @RequiresBusinessOrHrAdmin
    public static class AssignBusinessRoleRequest {
        @NotNull private UUID userId;
        @NotNull private UUID businessId;
        @NotBlank private String roleName;
        public UUID getUserId() { return userId; }
        public UUID getBusinessId() { return businessId; }
        public String getRoleName() { return roleName; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public void setBusinessId(UUID businessId) { this.businessId = businessId; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    @RequiresBusinessOrHrAdmin
    public static class RemoveBusinessRoleRequest {
        @NotNull private UUID userId;
        @NotNull private UUID businessId;
        @NotBlank private String roleName;
        public UUID getUserId() { return userId; }
        public UUID getBusinessId() { return businessId; }
        public String getRoleName() { return roleName; }
    }

    public static class AssignProjectRoleRequest {
        @NotNull private UUID userId;
        @NotNull private UUID projectId;
        @NotBlank private String roleName;
        public UUID getUserId() { return userId; }
        public UUID getProjectId() { return projectId; }
        public String getRoleName() { return roleName; }
    }

    public static class RemoveProjectRoleRequest {
        @NotNull private UUID userId;
        @NotNull private UUID projectId;
        @NotBlank private String roleName;
        public UUID getUserId() { return userId; }
        public UUID getProjectId() { return projectId; }
        public String getRoleName() { return roleName; }
    }
}
