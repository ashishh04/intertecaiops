package com.juviai.payroll.web;

import com.juviai.payroll.domain.ReimbursementStatus;
import com.juviai.payroll.dto.PayrollDtos.*;
import com.juviai.payroll.facade.PayrollFacade;
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
 * Employee self-service payroll controller.
 * Employees can view their own payslips, submit tax declarations,
 * and claim reimbursements.
 * All operations are scoped to the authenticated employee's ID extracted from JWT.
 */
@RestController
@RequestMapping("/api/v1/me/payroll")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeePayrollController {

    private final PayrollFacade facade;

    // ── Payslips ───────────────────────────────────────────────────────────

    /**
     * GET /api/v1/me/payroll/payslips
     * List all payslips for the authenticated employee (newest first).
     */
    @GetMapping("/payslips")
    public ResponseEntity<List<PayslipDto>> listMyPayslips(
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.listPayslipsByEmployee(employeeId));
    }

    /**
     * GET /api/v1/me/payroll/payslips/{year}/{month}
     * Fetch a specific month's payslip.
     */
    @GetMapping("/payslips/{year}/{month}")
    public ResponseEntity<PayslipDto> getMyPayslip(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.getPayslipByEmployeeAndPeriod(employeeId, year, month));
    }

    /**
     * GET /api/v1/me/payroll/payslips/{payslipId}
     * Fetch a payslip by ID (must belong to authenticated employee).
     */
    @GetMapping("/payslips/{payslipId}")
    public ResponseEntity<PayslipDto> getMyPayslipById(
            @PathVariable UUID payslipId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        PayslipDto payslip = facade.getPayslip(payslipId);
        // Ownership check
        if (!payslip.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(payslip);
    }

    // ── Tax Declarations ───────────────────────────────────────────────────

    /**
     * GET /api/v1/me/payroll/tax-declarations?financialYear=2025-26
     * List my tax declarations, optionally filtered by financial year.
     */
    @GetMapping("/tax-declarations")
    public ResponseEntity<List<TaxDeclarationDto>> listMyTaxDeclarations(
            @RequestParam(required = false) String financialYear,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.listTaxDeclarations(employeeId, financialYear));
    }

    /**
     * POST /api/v1/me/payroll/tax-declarations
     * Submit a new tax-saving declaration (e.g. 80C, HRA, 80D).
     */
    @PostMapping("/tax-declarations")
    public ResponseEntity<TaxDeclarationDto> submitTaxDeclaration(
            @Valid @RequestBody SubmitTaxDeclarationRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.submitTaxDeclaration(employeeId, request));
    }

    // ── Reimbursements ─────────────────────────────────────────────────────

    /**
     * GET /api/v1/me/payroll/reimbursements?status=PENDING
     * List my reimbursement claims, optionally filtered by status.
     */
    @GetMapping("/reimbursements")
    public ResponseEntity<List<ReimbursementDto>> listMyReimbursements(
            @RequestParam(required = false) ReimbursementStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.listReimbursements(employeeId, status));
    }

    /**
     * POST /api/v1/me/payroll/reimbursements
     * Submit a new expense reimbursement claim.
     */
    @PostMapping("/reimbursements")
    public ResponseEntity<ReimbursementDto> submitReimbursement(
            @Valid @RequestBody SubmitReimbursementRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID employeeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.submitReimbursement(employeeId, request));
    }
}
