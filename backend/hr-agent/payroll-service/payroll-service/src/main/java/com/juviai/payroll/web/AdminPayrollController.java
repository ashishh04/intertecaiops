package com.juviai.payroll.web;

import com.juviai.payroll.domain.PayrollPeriodStatus;
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
 * Admin / HR / Finance controller.
 * Manages payroll periods, salary structures, bulk payslip operations,
 * tax declaration reviews, and reimbursement approvals.
 */
@RestController
@RequestMapping("/api/v1/admin/payroll")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'FINANCE')")
public class AdminPayrollController {

    private final PayrollFacade facade;

    // ── Payroll Periods ────────────────────────────────────────────────────

    @PostMapping("/periods")
    public ResponseEntity<PayrollPeriodDto> createPeriod(
            @Valid @RequestBody CreatePayrollPeriodRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID createdBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.createPeriod(request, createdBy));
    }

    @GetMapping("/periods/{periodId}")
    public ResponseEntity<PayrollPeriodDto> getPeriod(@PathVariable UUID periodId) {
        return ResponseEntity.ok(facade.getPeriod(periodId));
    }

    @GetMapping("/periods")
    public ResponseEntity<List<PayrollPeriodDto>> listPeriods(
            @RequestParam UUID b2bUnitId,
            @RequestParam(required = false) PayrollPeriodStatus status) {
        return ResponseEntity.ok(facade.listPeriods(b2bUnitId, status));
    }

    @PostMapping("/periods/{periodId}/start-processing")
    public ResponseEntity<PayrollPeriodDto> startProcessing(
            @PathVariable UUID periodId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID initiatedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.startProcessing(periodId, initiatedBy));
    }

    @PostMapping("/periods/{periodId}/finalize")
    public ResponseEntity<PayrollPeriodDto> finalizePeriod(
            @PathVariable UUID periodId,
            @Valid @RequestBody FinalizePayrollRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID approvedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.finalizePeriod(periodId, approvedBy, request));
    }

    @PostMapping("/periods/{periodId}/mark-paid")
    public ResponseEntity<PayrollPeriodDto> markPaid(
            @PathVariable UUID periodId,
            @Valid @RequestBody MarkPaidRequestDto request) {
        return ResponseEntity.ok(facade.markPaid(periodId, request));
    }

    // ── Salary Structures ──────────────────────────────────────────────────

    @PostMapping("/salary-structures")
    public ResponseEntity<SalaryStructureDto> createSalaryStructure(
            @Valid @RequestBody CreateSalaryStructureRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.createSalaryStructure(request));
    }

    @GetMapping("/salary-structures/employee/{employeeId}/active")
    public ResponseEntity<SalaryStructureDto> getActiveSalaryStructure(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(facade.getActiveSalaryStructure(employeeId));
    }

    @GetMapping("/salary-structures/employee/{employeeId}/history")
    public ResponseEntity<List<SalaryStructureDto>> listSalaryStructureHistory(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(facade.listSalaryStructureHistory(employeeId));
    }

    // ── Payslips (Admin) ───────────────────────────────────────────────────

    @PostMapping("/periods/{periodId}/payslips/generate")
    public ResponseEntity<PayslipDto> generatePayslip(
            @PathVariable UUID periodId,
            @Valid @RequestBody GeneratePayslipRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.generatePayslip(periodId, request));
    }

    @PostMapping("/periods/{periodId}/payslips/bulk-generate")
    public ResponseEntity<List<PayslipDto>> bulkGeneratePayslips(
            @PathVariable UUID periodId,
            @Valid @RequestBody BulkGeneratePayslipRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.bulkGeneratePayslips(periodId, request));
    }

    @GetMapping("/periods/{periodId}/payslips")
    public ResponseEntity<List<PayslipDto>> listPayslipsByPeriod(@PathVariable UUID periodId) {
        return ResponseEntity.ok(facade.listPayslipsByPeriod(periodId));
    }

    @PostMapping("/payslips/{payslipId}/approve")
    public ResponseEntity<PayslipDto> approvePayslip(
            @PathVariable UUID payslipId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID approvedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.approvePayslip(payslipId, approvedBy));
    }

    @PostMapping("/payslips/{payslipId}/mark-paid")
    public ResponseEntity<PayslipDto> markPayslipPaid(
            @PathVariable UUID payslipId,
            @Valid @RequestBody MarkPayslipPaidRequestDto request) {
        return ResponseEntity.ok(facade.markPayslipPaid(payslipId, request));
    }

    @PostMapping("/payslips/{payslipId}/revise")
    public ResponseEntity<PayslipDto> revisePayslip(
            @PathVariable UUID payslipId,
            @RequestParam(required = false, defaultValue = "Payroll correction") String reason) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.revisePayslip(payslipId, reason));
    }

    // ── Tax Declarations (Review) ──────────────────────────────────────────

    @GetMapping("/tax-declarations/pending")
    public ResponseEntity<List<TaxDeclarationDto>> listPendingTaxDeclarations() {
        return ResponseEntity.ok(facade.listPendingTaxDeclarations());
    }

    @PostMapping("/tax-declarations/{id}/review")
    public ResponseEntity<TaxDeclarationDto> reviewTaxDeclaration(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewTaxDeclarationRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID reviewedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.reviewTaxDeclaration(id, request, reviewedBy));
    }

    // ── Reimbursements (Admin Review) ──────────────────────────────────────

    @GetMapping("/reimbursements/pending")
    public ResponseEntity<List<ReimbursementDto>> listPendingReimbursements() {
        return ResponseEntity.ok(facade.listPendingReimbursements());
    }

    @PostMapping("/reimbursements/{id}/approve")
    public ResponseEntity<ReimbursementDto> approveReimbursement(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveReimbursementRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID approvedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.approveReimbursement(id, request, approvedBy));
    }

    @PostMapping("/reimbursements/{id}/reject")
    public ResponseEntity<ReimbursementDto> rejectReimbursement(
            @PathVariable UUID id,
            @Valid @RequestBody RejectReimbursementRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID rejectedBy = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(facade.rejectReimbursement(id, request, rejectedBy));
    }

    @PostMapping("/reimbursements/settle")
    public ResponseEntity<List<ReimbursementDto>> settleReimbursements(
            @Valid @RequestBody SettleReimbursementsRequestDto request) {
        return ResponseEntity.ok(facade.settleReimbursements(request));
    }
}
