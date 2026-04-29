package com.juviai.user.dto;

import com.juviai.user.domain.RoleModule;
import lombok.Data;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Data
public class RoleData {
    private UUID id;
    private String name;
    private String description;
    private UUID b2bUnitId;

    /**
     * Functional modules this role is applicable to (HRMS, ECOMMERCE,
     * PROJECT_MANAGEMENT, ...). Empty when the role has not been tagged
     * against any specific module.
     */
    private Set<RoleModule> modules = EnumSet.noneOf(RoleModule.class);
}
