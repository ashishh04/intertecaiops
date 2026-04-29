package com.juviai.user.organisation.converter;

import com.juviai.user.converter.AbstractPopulatingConverter;
import com.juviai.user.organisation.converter.populator.DepartmentPopulator;
import com.juviai.user.organisation.domain.Department;
import com.juviai.user.organisation.web.dto.DepartmentDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartmentConverter extends AbstractPopulatingConverter<Department, DepartmentDTO> {

    public DepartmentConverter(List<DepartmentPopulator> populators) {
        super(populators, DepartmentDTO.class);
    }
}
