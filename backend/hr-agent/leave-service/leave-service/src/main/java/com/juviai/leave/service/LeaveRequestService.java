package com.juviai.leave.service;

import com.juviai.leave.domain.LeaveRequest;
import com.juviai.leave.domain.LeaveRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestService {

    /**
     * Apply for leave. Validates:
     *  - No overlapping approved/pending requests
     *  - Sufficient balance (or marks as LOP if unpaid type)
     *  - Document required check
     *  - Max consecutive days policy
     *  Reserves balance on submission.
     */
    LeaveRequest apply(LeaveRequest request);

    LeaveRequest getById(UUID id);

    List<LeaveRequest> listByEmployee(UUID employeeId);

    List<LeaveRequest> listByEmployeeAndStatus(UUID employeeId, LeaveRequestStatus status);

    List<LeaveRequest> listPendingByOrg(UUID b2bUnitId);

    /**
     * Approve a leave request. Moves reserved → used in balance.
     */
    LeaveRequest approve(UUID id, UUID approvedBy);

    /**
     * Reject a leave request. Releases reserved balance.
     */
    LeaveRequest reject(UUID id, String reason, UUID rejectedBy);

    /**
     * Employee cancels their own approved leave (only before fromDate).
     * Returns used balance back.
     */
    LeaveRequest cancel(UUID id, UUID employeeId);

    /**
     * HR revokes an approved leave (even after start date, e.g. emergency recall).
     */
    LeaveRequest revoke(UUID id, String reason, UUID revokedBy);

    /**
     * Returns LOP days for an employee in a given month.
     * Called by payroll-service during payslip generation.
     */
    BigDecimal getLopDays(UUID employeeId, int year, int month);

    /**
     * Count actual working days between two dates (excludes weekends and org holidays).
     */
    BigDecimal countWorkingDays(UUID b2bUnitId, LocalDate from, LocalDate to);
}
