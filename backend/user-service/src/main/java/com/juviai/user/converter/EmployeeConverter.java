package com.juviai.user.converter;

import com.juviai.user.converter.populator.EmployeePopulator;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.EmployeeData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeConverter extends AbstractPopulatingConverter<Employee, EmployeeData> {

    public EmployeeConverter(List<EmployeePopulator> populators) {
        super(populators, EmployeeData.class);
    }
}
