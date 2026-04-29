package com.juviai.user.facade;

import com.juviai.user.domain.RoleModule;
import com.juviai.user.dto.RoleData;
import java.util.List;
import java.util.UUID;

public interface RoleFacade {
    // Role CRUD
    RoleData createRole(String name, String description, UUID b2bUnitId);
    RoleData getRoleById(UUID roleId);
    RoleData updateRole(UUID roleId, String name, String description);
    void deleteRole(UUID roleId);
    List<RoleData> listSystemRoles(UUID b2bUnitId);
    List<RoleData> listBusinessRoles(UUID b2bUnitId);

    // Module-scoped listing — drives the module-aware admin UIs
    List<RoleData> listRolesByModule(RoleModule module);
    List<RoleData> listRolesByModule(RoleModule module, UUID b2bUnitId);

    // Business Role Assignments
    void assignBusinessRole(UUID userId, UUID businessId, String roleName, String assignedBy);
    void removeBusinessRole(UUID userId, UUID businessId, String roleName);
    List<RoleData> getUserRolesInBusiness(UUID userId, UUID businessId);

    // Project Role Assignments
    void assignProjectRole(UUID userId, UUID projectId, String roleName, String assignedBy);
    void removeProjectRole(UUID userId, UUID projectId, String roleName);
    List<RoleData> getUserRolesInProject(UUID userId, UUID projectId);
}
