package com.juviai.leave.service;

import com.juviai.leave.domain.CompOff;
import com.juviai.leave.domain.CompOffStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CompOffService {
    CompOff request(CompOff compOff);
    CompOff getById(UUID id);
    List<CompOff> listByEmployee(UUID employeeId);
    List<CompOff> listPending();
    CompOff approve(UUID id, UUID approvedBy);
    CompOff reject(UUID id, UUID rejectedBy);
    BigDecimal availableCredits(UUID employeeId);

    /** Scheduler: expire approved comp-offs past their expiry date */
    void expireStale();
}
