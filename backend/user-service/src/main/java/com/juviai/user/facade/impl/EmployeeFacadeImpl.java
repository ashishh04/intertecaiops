package com.juviai.user.facade.impl;

import com.juviai.user.converter.EmployeeConverter;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.EmploymentType;
import com.juviai.user.dto.CreateEmployeeRequestDto;
import com.juviai.user.dto.EmployeeData;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.dto.PageResponse;
import com.juviai.user.dto.UpdateBankAccountRequestDto;
import com.juviai.user.dto.UpdateEmployeeRequestDto;
import com.juviai.user.facade.EmployeeFacade;
import com.juviai.user.security.B2BUnitAccessValidator;
import com.juviai.user.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeFacadeImpl implements EmployeeFacade {

    private final EmployeeService employeeService;
    private final EmployeeConverter employeeConverter;
    private final B2BUnitAccessValidator b2bUnitAccessValidator;

    @Override
    public PageResponse<EmployeeData> search(UUID b2bUnitId, String q,
            EmploymentType employmentType, Pageable pageable) {
        b2bUnitAccessValidator.validateCurrentUserBelongsTo(b2bUnitId);
        Page<Employee> page = employeeService.search(b2bUnitId, q, employmentType, pageable);
        List<EmployeeData> items = page.getContent().stream()
                .map(employeeConverter::convert)
                .collect(Collectors.toList());
        return new PageResponse<>(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public EmployeeDetailsDto get(UUID id) {
        return employeeService.getDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    @Override
    public List<EmployeeData> listByB2b(UUID b2bUnitId) {
        return employeeConverter.convertAll(employeeService.listByB2bUnit(b2bUnitId));
    }

    @Override
    public EmployeeData create(CreateEmployeeRequestDto req) {
        Employee e = employeeService.create(req);
        return employeeConverter.convert(e);
    }

    @Override
    public EmployeeData update(UUID id, UpdateEmployeeRequestDto req) {
        Employee e = employeeService.update(id, req);
        return employeeConverter.convert(e);
    }

    @Override
    public void updateBankAccount(UUID id, UpdateBankAccountRequestDto req) {
        employeeService.updateBankAccount(id, req);
    }

    @Override
    public void deleteEmployee(UUID id) {
        employeeService.deleteUser(id);
    }

    @Override
    public EmployeeData setDismissed(UUID id, Boolean dismissed) {
        Employee e = employeeService.setDismissed(id, dismissed == null ? Boolean.TRUE : dismissed);
        return employeeConverter.convert(e);
    }

    @Override
    public EmployeeData updateOtherInformation(UUID id, UpdateEmployeeRequestDto req) {
        employeeService.getDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return null;
    }
}
