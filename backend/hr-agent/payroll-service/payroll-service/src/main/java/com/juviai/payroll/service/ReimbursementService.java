package com.juviai.payroll.service;

import com.juviai.payroll.domain.Reimbursement;
import com.juviai.payroll.domain.ReimbursementStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ReimbursementService {

    Reimbursement submit(Reimbursement reimbursement);

    Reimbursement getById(UUID id);

    List<Reimbursement> listByEmployee(UUID employeeId);

    List<Reimbursement> listByEmployeeAndStatus(UUID employeeId, ReimbursementStatus status);

    List<Reimbursement> listPending();

    Reimbursement approve(UUID id, BigDecimal approvedAmount, UUID approvedBy);

    Reimbursement reject(UUID id, String remarks, UUID rejectedBy);

    /**
     * Mark approved reimbursements as PAID and link them to the payroll period
     * in which they are settled.
     */
    List<Reimbursement> settleInPeriod(List<UUID> reimbursementIds, UUID periodId);
}
