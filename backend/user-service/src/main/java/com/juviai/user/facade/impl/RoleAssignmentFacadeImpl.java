package com.juviai.user.facade.impl;

import com.juviai.user.converter.RoleAssignmentConverter;
import com.juviai.user.domain.ScopeType;
import com.juviai.user.dto.RoleAssignmentData;
import com.juviai.user.dto.RoleAssignmentRequest;
import com.juviai.user.facade.RoleAssignmentFacade;
import com.juviai.user.service.RoleAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleAssignmentFacadeImpl implements RoleAssignmentFacade {

    private final RoleAssignmentService roleAssignmentService;
    private final RoleAssignmentConverter roleAssignmentConverter;

    @Override
    public RoleAssignmentData assign(RoleAssignmentRequest request, String assignedBy) {
        return roleAssignmentConverter.convert(roleAssignmentService.assign(
                request.getUserId(),
                request.getRoleName(),
                request.getScopeType(),
                request.getScopeId(),
                assignedBy,
                request.getExpiresAt()
        ));
    }

    @Override
    public void revoke(UUID userId, String roleName, ScopeType scopeType, UUID scopeId) {
        roleAssignmentService.revoke(userId, roleName, scopeType, scopeId);
    }

    @Override
    public RoleAssignmentData deactivate(UUID userId, String roleName, ScopeType scopeType, UUID scopeId) {
        return roleAssignmentConverter.convert(
                roleAssignmentService.deactivate(userId, roleName, scopeType, scopeId));
    }

    @Override
    public List<RoleAssignmentData> listForUser(UUID userId, boolean activeOnly) {
        return roleAssignmentConverter.convertAll(activeOnly
                ? roleAssignmentService.getEffectiveAssignmentsForUser(userId)
                : roleAssignmentService.getAllAssignmentsForUser(userId));
    }

    @Override
    public List<RoleAssignmentData> listAtScope(ScopeType scopeType, UUID scopeId) {
        return roleAssignmentConverter.convertAll(
                roleAssignmentService.getAssignmentsAtScope(scopeType, scopeId));
    }

    @Override
    public boolean hasRole(UUID userId, String roleName, ScopeType scopeType, UUID scopeId) {
        return roleAssignmentService.hasEffectiveRole(userId, roleName, scopeType, scopeId);
    }

    @Override
    public List<UUID> getScopeIdsForUserRole(UUID userId, String roleName, ScopeType scopeType) {
        return roleAssignmentService.getScopeIdsForUserRole(userId, roleName, scopeType);
    }
}
