package com.juviai.user.converter;

import com.juviai.user.converter.populator.EmployeeDetailsPopulator;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.EmployeeDetailsDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeDetailsConverter extends AbstractPopulatingConverter<Employee, EmployeeDetailsDto> {

    public EmployeeDetailsConverter(List<EmployeeDetailsPopulator> populators) {
        super(populators, EmployeeDetailsDto.class);
    }
}
