package com.juviai.user.web;

import com.juviai.user.domain.EmploymentType;
import com.juviai.user.dto.CreateEmployeeRequestDto;
import com.juviai.user.dto.EmployeeData;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.dto.PageResponse;
import com.juviai.user.dto.UpdateBankAccountRequestDto;
import com.juviai.user.dto.UpdateEmployeeRequestDto;
import com.juviai.user.facade.EmployeeFacade;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/employees")
@Validated
public class AdminEmployeeController {

    private final EmployeeFacade employeeFacade;

    public AdminEmployeeController(EmployeeFacade employeeFacade) {
        this.employeeFacade = employeeFacade;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponse<EmployeeData> search(
            @RequestParam("b2bUnitId") UUID b2bUnitId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "employmentType", required = false) EmploymentType employmentType,
            Pageable pageable) {
        return employeeFacade.search(b2bUnitId, q, employmentType, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EmployeeDetailsDto get(@PathVariable("id") @NonNull UUID id) {
        return employeeFacade.get(id);
    }

    @GetMapping("/byb2b/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public List<EmployeeData> listByB2b(@PathVariable("b2bUnitId") UUID b2bUnitId) {
        return employeeFacade.listByB2b(b2bUnitId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeData create(@RequestBody CreateEmployeeRequestDto req) {
        return employeeFacade.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EmployeeData update(@PathVariable("id") @NonNull UUID id,
                               @RequestBody UpdateEmployeeRequestDto req) {
        return employeeFacade.update(id, req);
    }

    @PutMapping("/{id}/bank-account")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBankAccount(@PathVariable("id") @NonNull UUID id,
                                  @RequestBody UpdateBankAccountRequestDto req) {
        employeeFacade.updateBankAccount(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @NonNull UUID id) {
        employeeFacade.deleteEmployee(id);
    }

    @PatchMapping("/{id}/dismiss")
    @PreAuthorize("isAuthenticated()")
    public EmployeeData dismissEmployee(
            @PathVariable("id") @NonNull UUID id,
            @RequestParam(value = "dismissed", required = false) Boolean dismissed) {
        return employeeFacade.setDismissed(id, dismissed);
    }
}
