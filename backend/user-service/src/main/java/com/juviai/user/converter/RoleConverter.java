package com.juviai.user.converter;

import com.juviai.user.converter.populator.RolePopulator;
import com.juviai.user.domain.Role;
import com.juviai.user.dto.RoleData;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RoleConverter extends AbstractPopulatingConverter<Role, RoleData> {
    public RoleConverter(List<RolePopulator> populators) {
        super(populators, RoleData.class);
    }
}
