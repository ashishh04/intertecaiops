package com.juviai.user.facade.impl;

import com.juviai.user.converter.CompensationConverter;
import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.CompensationDto;
import com.juviai.user.dto.CreateCompensationRequestDto;
import com.juviai.user.facade.CompensationFacade;
import com.juviai.user.repo.EmployeeRepository;
import com.juviai.user.service.CompensationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompensationFacadeImpl implements CompensationFacade {

    private final CompensationService compensationService;
    private final CompensationConverter compensationConverter;
    private final EmployeeRepository employeeRepository;

    @Override
    public CompensationDto get(UUID id) {
        Compensation c = compensationService.getById(id);
        return compensationConverter.convert(c);
    }

    @Override
    public List<CompensationDto> getAll() {
        return compensationConverter.convertAll(compensationService.getAll());
    }

    @Override
    public List<CompensationDto> listHistory(UUID employeeId) {
        return compensationConverter.convertAll(compensationService.listHistory(employeeId));
    }

    @Override
    public CompensationDto create(CreateCompensationRequestDto req) {
        if (req == null) {
            throw new IllegalArgumentException("Request is required");
        }

        if (req.getEmployeeId() == null) throw new IllegalArgumentException("employeeId is required");
        if (req.getType() == null) throw new IllegalArgumentException("type is required");
        if (req.getAmount() == null) throw new IllegalArgumentException("amount is required");

        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Compensation c = new Compensation();
        c.setEmployee(employee);
        c.setType(req.getType());
        c.setAmount(req.getAmount());
        if (req.getEffectiveStartDate() != null) {
            c.setEffectiveStartDate(req.getEffectiveStartDate());
        } else if (employee.getHireDate() != null) {
            c.setEffectiveStartDate(employee.getHireDate());
        } else {
            throw new IllegalArgumentException("effectiveStartDate is required (or employee hireDate must be set)");
        }
        c.setEffectiveEndDate(null);
        c.setActive(true);

        Compensation created;
        if (compensationService.getActive(req.getEmployeeId(), req.getType()) == null) {
            created = compensationService.create(c);
        } else {
            created = compensationService.reviseActiveCompensation(req.getEmployeeId(), req.getType(), c);
        }
        return compensationConverter.convert(created);
    }
}
