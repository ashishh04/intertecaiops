package com.juviai.user.converter.populator.employee;

import com.juviai.common.orm.BaseEntity;
import com.juviai.user.converter.populator.EmployeeDetailsPopulator;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.Role;
import com.juviai.user.dto.EmployeeDetailsDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EmployeeDetailsRolesPopulator implements EmployeeDetailsPopulator {

    @Override
    public void populate(Employee source, EmployeeDetailsDto target) {
        List<UUID> roleIds = source.getRoles() == null ? List.of() : source.getRoles().stream()
                .map(BaseEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        java.util.Set<String> rolesSet = source.getRoles() == null ? java.util.Set.of() : source.getRoles().stream()
                .map(Role::getName)
                .filter(x -> x != null && !x.isBlank())
                .collect(Collectors.toSet());
        if (rolesSet.contains("ADMIN")) {
            rolesSet.add("ROLE_ADMIN");
        }
        List<String> roles = rolesSet.stream().sorted().toList();

        target.setRoleIds(roleIds);
        target.setRoles(roles);
    }
}
