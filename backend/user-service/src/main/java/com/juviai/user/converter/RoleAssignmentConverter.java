package com.juviai.user.converter;

import com.juviai.user.converter.populator.RoleAssignmentPopulator;
import com.juviai.user.domain.RoleAssignment;
import com.juviai.user.dto.RoleAssignmentData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleAssignmentConverter extends AbstractPopulatingConverter<RoleAssignment, RoleAssignmentData> {
    public RoleAssignmentConverter(List<RoleAssignmentPopulator> populators) {
        super(populators, RoleAssignmentData.class);
    }
}
