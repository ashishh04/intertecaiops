package com.juviai.user.facade;

import com.juviai.user.domain.EmploymentType;
import com.juviai.user.dto.CreateEmployeeRequestDto;
import com.juviai.user.dto.EmployeeData;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.dto.PageResponse;
import com.juviai.user.dto.UpdateBankAccountRequestDto;
import com.juviai.user.dto.UpdateEmployeeRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EmployeeFacade {
    PageResponse<EmployeeData> search(UUID b2bUnitId, String q, EmploymentType employmentType, Pageable pageable);
    EmployeeDetailsDto get(UUID id);
    List<EmployeeData> listByB2b(UUID b2bUnitId);
    EmployeeData create(CreateEmployeeRequestDto req);
    EmployeeData update(UUID id, UpdateEmployeeRequestDto req);
    void updateBankAccount(UUID id, UpdateBankAccountRequestDto req);
    void deleteEmployee(UUID id);
    EmployeeData setDismissed(UUID id, Boolean dismissed);
    EmployeeData updateOtherInformation(UUID id, UpdateEmployeeRequestDto req);
}
