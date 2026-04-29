package com.juviai.payroll.service;

import com.juviai.payroll.domain.PayrollPeriod;
import com.juviai.payroll.domain.PayrollPeriodStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PayrollPeriodService {

    PayrollPeriod create(UUID b2bUnitId, int year, int month, UUID createdBy);

    PayrollPeriod getById(UUID id);

    List<PayrollPeriod> listByOrg(UUID b2bUnitId);

    List<PayrollPeriod> listByOrgAndStatus(UUID b2bUnitId, PayrollPeriodStatus status);

    /**
     * Transition: DRAFT → PROCESSING.
     * Triggers payslip generation for all active employees in the org.
     */
    PayrollPeriod startProcessing(UUID periodId, UUID initiatedBy);

    /**
     * Transition: PROCESSING → FINALIZED.
     * Locks all payslips; no further edits allowed.
     */
    PayrollPeriod finalize(UUID periodId, UUID approvedBy);

    /**
     * Transition: FINALIZED → PAID.
     * Records actual payment date and payment reference.
     */
    PayrollPeriod markPaid(UUID periodId, LocalDate paymentDate, String remarks);
}
