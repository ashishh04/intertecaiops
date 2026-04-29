package com.juviai.user.web;

import com.juviai.user.dto.CompensationDto;
import com.juviai.user.dto.CreateCompensationRequestDto;
import com.juviai.user.facade.CompensationFacade;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/compensations")
@Validated
public class AdminCompensationController {

    private final CompensationFacade compensationFacade;

    public AdminCompensationController(CompensationFacade compensationFacade) {
        this.compensationFacade = compensationFacade;
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CompensationDto get(@PathVariable("id") @NonNull UUID id) {
        return compensationFacade.get(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CompensationDto> getAll(@RequestParam(value = "employeeId", required = false) UUID employeeId) {
        if (employeeId != null) {
            return compensationFacade.listHistory(employeeId);
        }
        return compensationFacade.getAll();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CompensationDto create(@RequestBody CreateCompensationRequestDto req) {
        return compensationFacade.create(req);
    }
}
