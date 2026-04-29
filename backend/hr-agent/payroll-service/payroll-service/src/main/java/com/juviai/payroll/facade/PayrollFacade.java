package com.juviai.payroll.facade;

import com.juviai.payroll.dto.PayrollDtos.*;
import com.juviai.payroll.domain.PayrollPeriodStatus;
import com.juviai.payroll.domain.ReimbursementStatus;

import java.util.List;
import java.util.UUID;

public interface PayrollFacade {

    // ── Payroll Periods ────────────────────────────────────────────────────
    PayrollPeriodDto createPeriod(CreatePayrollPeriodRequestDto request, UUID createdBy);
    PayrollPeriodDto getPeriod(UUID periodId);
    List<PayrollPeriodDto> listPeriods(UUID b2bUnitId, PayrollPeriodStatus status);
    PayrollPeriodDto startProcessing(UUID periodId, UUID initiatedBy);
    PayrollPeriodDto finalizePeriod(UUID periodId, UUID approvedBy, FinalizePayrollRequestDto request);
    PayrollPeriodDto markPaid(UUID periodId, MarkPaidRequestDto request);

    // ── Salary Structures ──────────────────────────────────────────────────
    SalaryStructureDto createSalaryStructure(CreateSalaryStructureRequestDto request);
    SalaryStructureDto getActiveSalaryStructure(UUID employeeId);
    List<SalaryStructureDto> listSalaryStructureHistory(UUID employeeId);

    // ── Payslips ───────────────────────────────────────────────────────────
    PayslipDto generatePayslip(UUID periodId, GeneratePayslipRequestDto request);
    List<PayslipDto> bulkGeneratePayslips(UUID periodId, BulkGeneratePayslipRequestDto request);
    PayslipDto getPayslip(UUID payslipId);
    List<PayslipDto> listPayslipsByPeriod(UUID periodId);
    List<PayslipDto> listPayslipsByEmployee(UUID employeeId);
    PayslipDto getPayslipByEmployeeAndPeriod(UUID employeeId, int year, int month);
    PayslipDto approvePayslip(UUID payslipId, UUID approvedBy);
    PayslipDto markPayslipPaid(UUID payslipId, MarkPayslipPaidRequestDto request);
    PayslipDto revisePayslip(UUID payslipId, String reason);

    // ── Tax Declarations ───────────────────────────────────────────────────
    TaxDeclarationDto submitTaxDeclaration(UUID employeeId, SubmitTaxDeclarationRequestDto request);
    List<TaxDeclarationDto> listTaxDeclarations(UUID employeeId, String financialYear);
    List<TaxDeclarationDto> listPendingTaxDeclarations();
    TaxDeclarationDto reviewTaxDeclaration(UUID id, ReviewTaxDeclarationRequestDto request, UUID reviewedBy);

    // ── Reimbursements ─────────────────────────────────────────────────────
    ReimbursementDto submitReimbursement(UUID employeeId, SubmitReimbursementRequestDto request);
    List<ReimbursementDto> listReimbursements(UUID employeeId, ReimbursementStatus status);
    List<ReimbursementDto> listPendingReimbursements();
    ReimbursementDto approveReimbursement(UUID id, ApproveReimbursementRequestDto request, UUID approvedBy);
    ReimbursementDto rejectReimbursement(UUID id, RejectReimbursementRequestDto request, UUID rejectedBy);
    List<ReimbursementDto> settleReimbursements(SettleReimbursementsRequestDto request);
}
