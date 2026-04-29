package com.juviai.payroll.service;

import com.juviai.payroll.domain.Payslip;
import com.juviai.payroll.domain.PayslipStatus;

import java.util.List;
import java.util.UUID;

public interface PayslipService {

    Payslip getById(UUID id);

    List<Payslip> listByPeriod(UUID periodId);

    List<Payslip> listByEmployee(UUID employeeId);

    Payslip getByEmployeeAndPeriod(UUID employeeId, int year, int month);

    /**
     * Generates a payslip for one employee in a given period.
     * Uses the active salary structure + LOP days from leave-service.
     * Idempotent: if a DRAFT payslip already exists it is regenerated.
     */
    Payslip generate(UUID periodId, UUID employeeId, int lopDays);

    /**
     * Approve a payslip (locks components, prevents regeneration).
     */
    Payslip approve(UUID payslipId, UUID approvedBy);

    /**
     * Mark a payslip as PAID and record the payment reference.
     */
    Payslip markPaid(UUID payslipId, String paymentReference);

    /**
     * Revise a PAID payslip (creates a REVISED copy, marks original as superseded).
     * Used for payroll corrections.
     */
    Payslip revise(UUID payslipId, String reason);
}
