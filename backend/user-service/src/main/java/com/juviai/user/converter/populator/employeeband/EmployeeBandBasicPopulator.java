package com.juviai.user.converter.populator.employeeband;

import com.juviai.user.converter.populator.EmployeeBandPopulator;
import com.juviai.user.domain.EmployeeOrgBand;
import com.juviai.user.web.dto.EmployeeBandResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class EmployeeBandBasicPopulator implements EmployeeBandPopulator {
    @Override
    public void populate(EmployeeOrgBand source, EmployeeBandResponseDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setExperienceMin(source.getExperienceMin());
        target.setExperienceMax(source.getExperienceMax());
        target.setStartingSalary(source.getStartingSalary());
        target.setEndingSalary(source.getEndingSalary());
        if (source.getB2bUnit() != null) {
            target.setB2bUnitId(source.getB2bUnit().getId());
        }
    }
}
