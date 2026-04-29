package com.juviai.user.converter.populator.employee;

import com.juviai.user.converter.CompensationConverter;
import com.juviai.user.converter.populator.EmployeeDetailsPopulator;
import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.CompensationDto;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.service.CompensationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeDetailsCompensationPopulator implements EmployeeDetailsPopulator {

    private final CompensationService compensationService;
    private final CompensationConverter compensationConverter;

    @Override
    public void populate(Employee source, EmployeeDetailsDto target) {
        Compensation active = compensationService.getActive(source.getId());
        target.setActiveCompensation(compensationConverter.convert(active));

        List<CompensationDto> history = compensationConverter.convertAll(compensationService.listHistory(source.getId()));
        target.setCompensationHistory(history);
    }
}
