package com.juviai.user.converter.populator.roleassignment;

import com.juviai.user.converter.populator.RoleAssignmentPopulator;
import com.juviai.user.domain.RoleAssignment;
import com.juviai.user.dto.RoleAssignmentData;
import org.springframework.stereotype.Component;

@Component
public class RoleAssignmentBasicPopulator implements RoleAssignmentPopulator {

    @Override
    public void populate(RoleAssignment source, RoleAssignmentData target) {
        target.setId(source.getId());

        if (source.getUser() != null) {
            target.setUserId(source.getUser().getId());
            target.setUserEmail(source.getUser().getEmail());
        }

        if (source.getRole() != null) {
            target.setRoleId(source.getRole().getId());
            target.setRoleName(source.getRole().getName());
        }

        target.setScopeType(source.getScopeType());
        target.setScopeId(source.getScopeId());
        target.setAssignedBy(source.getAssignedBy());
        target.setExpiresAt(source.getExpiresAt());
        target.setActive(source.isActive());
        target.setEffective(source.isEffective());
    }
}
