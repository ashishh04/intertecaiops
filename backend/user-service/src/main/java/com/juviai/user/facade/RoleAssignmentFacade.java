package com.juviai.user.facade;

import com.juviai.user.domain.ScopeType;
import com.juviai.user.dto.RoleAssignmentData;
import com.juviai.user.dto.RoleAssignmentRequest;

import java.util.List;
import java.util.UUID;

/**
 * Facade for the generic scoped-role-assignment API. Backed by
 * {@link com.juviai.user.service.RoleAssignmentService}.
 */
public interface RoleAssignmentFacade {

    RoleAssignmentData assign(RoleAssignmentRequest request, String assignedBy);

    void revoke(UUID userId, String roleName, ScopeType scopeType, UUID scopeId);

    RoleAssignmentData deactivate(UUID userId, String roleName, ScopeType scopeType, UUID scopeId);

    List<RoleAssignmentData> listForUser(UUID userId, boolean activeOnly);

    List<RoleAssignmentData> listAtScope(ScopeType scopeType, UUID scopeId);

    boolean hasRole(UUID userId, String roleName, ScopeType scopeType, UUID scopeId);

    List<UUID> getScopeIdsForUserRole(UUID userId, String roleName, ScopeType scopeType);
}
