package com.juviai.leave.web;

import com.juviai.leave.domain.LeaveRequestStatus;
import com.juviai.leave.dto.LeaveDtos.*;
import com.juviai.leave.facade.LeaveFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin / HR controller for leave management.
 * Covers: leave type setup, policy config, holiday calendar,
 * org-wide pending request review, balance initialization, comp-off approvals.
 */
@RestController
@RequestMapping("/api/v1/admin/leave")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
public class AdminLeaveController {

    private final LeaveFacade facade;

    // ── Leave Types ────────────────────────────────────────────────────────

    @PostMapping("/types")
    public ResponseEntity<LeaveTypeDto> createLeaveType(
            @Valid @RequestBody CreateLeaveTypeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.createLeaveType(request));
    }

    @GetMapping("/types")
    public ResponseEntity<List<LeaveTypeDto>> listLeaveTypes(@RequestParam UUID b2bUnitId) {
        return ResponseEntity.ok(facade.listLeaveTypes(b2bUnitId));
    }

    // ── Leave Policies ─────────────────────────────────────────────────────

    @PostMapping("/policies")
    public ResponseEntity<LeavePolicyDto> createPolicy(
            @Valid @RequestBody CreateLeavePolicyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.createPolicy(request));
    }

    @GetMapping("/policies")
    public ResponseEntity<List<LeavePolicyDto>> listPolicies(@RequestParam UUID b2bUnitId) {
        return ResponseEntity.ok(facade.listPolicies(b2bUnitId));
    }

    // ── Leave Balances ─────────────────────────────────────────────────────

    @PostMapping("/balances/initialize")
    public ResponseEntity<List<LeaveBalanceDto>> initializeBalances(
            @RequestParam UUID employeeId,
            @RequestParam UUID b2bUnitId,
            @RequestParam int  year) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.initializeBalances(employeeId, b2bUnitId, year));
    }

    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalanceDto>> listBalances(
            @RequestParam UUID employeeId,
            @RequestParam int  year) {
        return ResponseEntity.ok(facade.listBalances(employeeId, year));
    }

    // ── Leave Requests (Admin Review) ──────────────────────────────────────

    @GetMapping("/requests/pending")
    public ResponseEntity<List<LeaveRequestDto>> listPendingRequests(@RequestParam UUID b2bUnitId) {
        return ResponseEntity.ok(facade.listPendingForOrg(b2bUnitId));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<LeaveRequestDto> getRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(facade.getRequest(id));
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<LeaveRequestDto> approveRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID approvedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.approve(id, approvedBy));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<LeaveRequestDto> rejectRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RejectLeaveRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID rejectedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.reject(id, request, rejectedBy));
    }

    @PostMapping("/requests/{id}/revoke")
    public ResponseEntity<LeaveRequestDto> revokeRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RevokeLeaveRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID revokedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.revoke(id, request, revokedBy));
    }

    // ── Public Holidays ────────────────────────────────────────────────────

    @PostMapping("/holidays")
    public ResponseEntity<HolidayDto> createHoliday(
            @Valid @RequestBody CreateHolidayRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.createHoliday(request));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayDto>> listHolidays(
            @RequestParam UUID b2bUnitId,
            @RequestParam int year) {
        return ResponseEntity.ok(facade.listHolidays(b2bUnitId, year));
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        facade.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    // ── Comp Offs (Admin Review) ───────────────────────────────────────────

    @GetMapping("/comp-offs/pending")
    public ResponseEntity<List<CompOffDto>> listPendingCompOffs() {
        return ResponseEntity.ok(facade.listPendingCompOffs());
    }

    @PostMapping("/comp-offs/{id}/approve")
    public ResponseEntity<CompOffDto> approveCompOff(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(facade.approveCompOff(id, UUID.fromString(jwt.getSubject())));
    }

    @PostMapping("/comp-offs/{id}/reject")
    public ResponseEntity<CompOffDto> rejectCompOff(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(facade.rejectCompOff(id, UUID.fromString(jwt.getSubject())));
    }

    // ── Payroll Integration ────────────────────────────────────────────────

    /**
     * GET /api/v1/admin/leave/lop?employeeId=...&year=2026&month=4
     * Called by payroll-service during payslip generation to get LOP days.
     */
    @GetMapping("/lop")
    public ResponseEntity<LopSummaryDto> getLopSummary(
            @RequestParam UUID employeeId,
            @RequestParam int  year,
            @RequestParam int  month) {
        return ResponseEntity.ok(facade.getLopSummary(employeeId, year, month));
    }
}
