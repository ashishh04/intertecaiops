package com.juviai.leave.service;

import com.juviai.leave.domain.LeaveBalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LeaveBalanceService {

    /** Initialize balances for all leave types for a new employee joining mid-year */
    List<LeaveBalance> initializeForEmployee(UUID employeeId, UUID b2bUnitId, int year);

    LeaveBalance getBalance(UUID employeeId, UUID leaveTypeId, int year);

    List<LeaveBalance> listBalances(UUID employeeId, int year);

    /** Credit additional days (manual adjustment or accrual) */
    LeaveBalance credit(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days);

    /** Reserve days when a request is PENDING approval */
    LeaveBalance reserve(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days);

    /** Release reserved days when a request is REJECTED or CANCELLED */
    LeaveBalance release(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days);

    /** Consume reserved days when a request is APPROVED (moves pending → used) */
    LeaveBalance consume(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days);

    /** Add Loss-of-Pay days to balance (used by payroll) */
    LeaveBalance addLop(UUID employeeId, UUID leaveTypeId, int year, BigDecimal lopDays);

    /** Carry forward eligible unused balance into the next year */
    List<LeaveBalance> carryForward(UUID b2bUnitId, int fromYear);

    /** Monthly accrual job: credit 1/12th of annual entitlement for MONTHLY accrual types */
    void accrueMonthly(UUID b2bUnitId, int year, int month);
}
