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

import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Employee self-service leave controller.
 * Employees can view balances, apply for leave, cancel requests, request comp-offs.
 * All operations are scoped to the authenticated employee's JWT subject.
 */
@RestController
@RequestMapping("/api/v1/me/leave")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeLeaveController {

    private final LeaveFacade facade;

    // ── Leave Balances ─────────────────────────────────────────────────────

    /**
     * GET /api/v1/me/leave/balances?year=2026
     * Returns all leave type balances for the current year.
     */
    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalanceDto>> myBalances(
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal Jwt jwt) {
        int resolvedYear = (year == 0) ? Year.now().getValue() : year;
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.listBalances(employeeId, resolvedYear));
    }

    // ── Leave Requests ─────────────────────────────────────────────────────

    /**
     * GET /api/v1/me/leave/requests?status=PENDING
     * List my leave requests, optionally filtered by status.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequestDto>> myRequests(
            @RequestParam(required = false) LeaveRequestStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.listMyRequests(employeeId, status));
    }

    /**
     * GET /api/v1/me/leave/requests/{id}
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<LeaveRequestDto> getRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        LeaveRequestDto dto = facade.getRequest(id);
        UUID employeeId = UUID.fromString(jwt.getSubject());
        if (!dto.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/v1/me/leave/requests
     * Apply for leave.
     */
    @PostMapping("/requests")
    public ResponseEntity<LeaveRequestDto> applyLeave(
            @Valid @RequestBody ApplyLeaveRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.apply(employeeId, request));
    }

    /**
     * POST /api/v1/me/leave/requests/{id}/cancel
     * Cancel a pending or approved (future) leave request.
     */
    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<LeaveRequestDto> cancelRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.cancel(id, employeeId));
    }

    // ── Holidays ───────────────────────────────────────────────────────────

    /**
     * GET /api/v1/me/leave/holidays?b2bUnitId=...&year=2026
     * View the org holiday calendar.
     */
    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayDto>> listHolidays(
            @RequestParam UUID b2bUnitId,
            @RequestParam(defaultValue = "0") int year) {
        int resolvedYear = (year == 0) ? Year.now().getValue() : year;
        return ResponseEntity.ok(facade.listHolidays(b2bUnitId, resolvedYear));
    }

    // ── Comp Offs ──────────────────────────────────────────────────────────

    /**
     * GET /api/v1/me/leave/comp-offs
     * View my comp-off credits.
     */
    @GetMapping("/comp-offs")
    public ResponseEntity<List<CompOffDto>> myCompOffs(@AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.listMyCompOffs(employeeId));
    }

    /**
     * POST /api/v1/me/leave/comp-offs
     * Request a comp-off for working on a weekend or holiday.
     */
    @PostMapping("/comp-offs")
    public ResponseEntity<CompOffDto> requestCompOff(
            @Valid @RequestBody RequestCompOffDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.requestCompOff(employeeId, request));
    }
}
