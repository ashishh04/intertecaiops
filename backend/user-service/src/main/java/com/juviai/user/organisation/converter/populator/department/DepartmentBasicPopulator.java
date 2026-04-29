package com.juviai.user.organisation.converter.populator.department;

import com.juviai.user.organisation.converter.populator.DepartmentPopulator;
import com.juviai.user.organisation.domain.Department;
import com.juviai.user.organisation.web.dto.DepartmentDTO;
import org.springframework.stereotype.Component;

@Component
public class DepartmentBasicPopulator implements DepartmentPopulator {

    @Override
    public void populate(Department source, DepartmentDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setActive(source.isActive());
        target.setCode(source.getCode());
        // b2bUnitId will be set via context in controller if needed
    }
}
