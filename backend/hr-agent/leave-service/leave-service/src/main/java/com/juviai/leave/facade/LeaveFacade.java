package com.juviai.leave.facade;

import com.juviai.leave.domain.LeaveRequestStatus;
import com.juviai.leave.dto.LeaveDtos.*;

import java.util.List;
import java.util.UUID;

public interface LeaveFacade {

    // ── Leave Types ────────────────────────────────────────────────────────
    LeaveTypeDto    createLeaveType(CreateLeaveTypeRequestDto request);
    List<LeaveTypeDto> listLeaveTypes(UUID b2bUnitId);

    // ── Leave Policies ─────────────────────────────────────────────────────
    LeavePolicyDto  createPolicy(CreateLeavePolicyRequestDto request);
    List<LeavePolicyDto> listPolicies(UUID b2bUnitId);

    // ── Leave Balances ─────────────────────────────────────────────────────
    List<LeaveBalanceDto> listBalances(UUID employeeId, int year);
    List<LeaveBalanceDto> initializeBalances(UUID employeeId, UUID b2bUnitId, int year);

    // ── Leave Requests ─────────────────────────────────────────────────────
    LeaveRequestDto apply(UUID employeeId, ApplyLeaveRequestDto request);
    LeaveRequestDto getRequest(UUID id);
    List<LeaveRequestDto> listMyRequests(UUID employeeId, LeaveRequestStatus status);
    List<LeaveRequestDto> listPendingForOrg(UUID b2bUnitId);
    LeaveRequestDto approve(UUID id, UUID approvedBy);
    LeaveRequestDto reject(UUID id, RejectLeaveRequestDto request, UUID rejectedBy);
    LeaveRequestDto cancel(UUID id, UUID employeeId);
    LeaveRequestDto revoke(UUID id, RevokeLeaveRequestDto request, UUID revokedBy);

    // ── Holidays ───────────────────────────────────────────────────────────
    HolidayDto      createHoliday(CreateHolidayRequestDto request);
    List<HolidayDto> listHolidays(UUID b2bUnitId, int year);
    void            deleteHoliday(UUID id);

    // ── Comp Offs ──────────────────────────────────────────────────────────
    CompOffDto      requestCompOff(UUID employeeId, RequestCompOffDto request);
    List<CompOffDto> listMyCompOffs(UUID employeeId);
    List<CompOffDto> listPendingCompOffs();
    CompOffDto      approveCompOff(UUID id, UUID approvedBy);
    CompOffDto      rejectCompOff(UUID id, UUID rejectedBy);

    // ── Payroll integration ────────────────────────────────────────────────
    LopSummaryDto   getLopSummary(UUID employeeId, int year, int month);
}
