package com.juviai.user.facade.impl;

import com.juviai.user.converter.RoleConverter;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.RoleModule;
import com.juviai.user.dto.RoleData;
import com.juviai.user.facade.RoleFacade;
import com.juviai.user.service.BusinessRoleService;
import com.juviai.user.service.ProjectRoleService;
import com.juviai.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleFacadeImpl implements RoleFacade {

    private final RoleService roleService;
    private final BusinessRoleService businessRoleService;
    private final ProjectRoleService projectRoleService;
    private final RoleConverter roleConverter;

    @Override
    public RoleData createRole(String name, String description, UUID b2bUnitId) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setB2bUnitId(b2bUnitId);
        return roleConverter.convert(roleService.createRole(role));
    }

    @Override
    public RoleData getRoleById(UUID roleId) {
        return roleConverter.convert(roleService.getRoleById(roleId));
    }

    @Override
    public RoleData updateRole(UUID roleId, String name, String description) {
        return roleConverter.convert(roleService.updateRole(roleId, name, description));
    }

    @Override
    public void deleteRole(UUID roleId) {
        roleService.deleteRole(roleId);
    }

    @Override
    public List<RoleData> listSystemRoles(UUID b2bUnitId) {
        return roleConverter.convertAll(roleService.getSystemRoles(b2bUnitId));
    }

    @Override
    public List<RoleData> listBusinessRoles(UUID b2bUnitId) {
        return roleConverter.convertAll(roleService.getRolesByBusiness(b2bUnitId));
    }

    @Override
    public List<RoleData> listRolesByModule(RoleModule module) {
        return roleConverter.convertAll(roleService.getRolesByModule(module));
    }

    @Override
    public List<RoleData> listRolesByModule(RoleModule module, UUID b2bUnitId) {
        return roleConverter.convertAll(roleService.getRolesByModuleForBusiness(module, b2bUnitId));
    }

    @Override
    public void assignBusinessRole(UUID userId, UUID businessId, String roleName, String assignedBy) {
        businessRoleService.assignRoleToUser(userId, businessId, roleName, assignedBy);
    }

    @Override
    public void removeBusinessRole(UUID userId, UUID businessId, String roleName) {
        businessRoleService.removeRoleFromUser(userId, businessId, roleName);
    }

    @Override
    public List<RoleData> getUserRolesInBusiness(UUID userId, UUID businessId) {
        return roleConverter.convertAll(businessRoleService.getUserRolesInBusiness(userId, businessId));
    }

    @Override
    public void assignProjectRole(UUID userId, UUID projectId, String roleName, String assignedBy) {
        projectRoleService.assignRoleToUser(userId, projectId, roleName, assignedBy);
    }

    @Override
    public void removeProjectRole(UUID userId, UUID projectId, String roleName) {
        projectRoleService.removeRoleFromUser(userId, projectId, roleName);
    }

    @Override
    public List<RoleData> getUserRolesInProject(UUID userId, UUID projectId) {
        return roleConverter.convertAll(projectRoleService.getUserRolesInProject(userId, projectId));
    }
}
