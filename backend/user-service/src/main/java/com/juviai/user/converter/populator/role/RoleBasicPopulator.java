package com.juviai.user.converter.populator.role;

import com.juviai.user.converter.populator.RolePopulator;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.RoleModule;
import com.juviai.user.dto.RoleData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class RoleBasicPopulator implements RolePopulator {
    @Override
    public void populate(Role source, RoleData target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setB2bUnitId(source.getB2bUnitId());
        target.setModules(
                (source.getModules() == null || source.getModules().isEmpty())
                        ? EnumSet.noneOf(RoleModule.class)
                        : EnumSet.copyOf(source.getModules())
        );
    }
}
