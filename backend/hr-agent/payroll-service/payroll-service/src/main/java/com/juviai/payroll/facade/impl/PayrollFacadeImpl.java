package com.juviai.payroll.facade.impl;

import com.juviai.payroll.converter.*;
import com.juviai.payroll.domain.*;
import com.juviai.payroll.dto.PayrollDtos.*;
import com.juviai.payroll.facade.PayrollFacade;
import com.juviai.payroll.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayrollFacadeImpl implements PayrollFacade {

    private final PayrollPeriodService    periodService;
    private final PayslipService          payslipService;
    private final SalaryStructureService  structureService;
    private final TaxDeclarationService   taxService;
    private final ReimbursementService    reimbursementService;

    private final PayrollPeriodConverter  periodConverter;
    private final PayslipConverter        payslipConverter;
    private final SalaryStructureConverter structureConverter;
    private final TaxDeclarationConverter  taxConverter;
    private final ReimbursementConverter   reimbursementConverter;

    // ── Payroll Periods ────────────────────────────────────────────────────

    @Override
    public PayrollPeriodDto createPeriod(CreatePayrollPeriodRequestDto request, UUID createdBy) {
        PayrollPeriod period = periodService.create(
                request.getB2bUnitId(), request.getYear(), request.getMonth(), createdBy);
        return periodConverter.convert(period);
    }

    @Override
    public PayrollPeriodDto getPeriod(UUID periodId) {
        return periodConverter.convert(periodService.getById(periodId));
    }

    @Override
    public List<PayrollPeriodDto> listPeriods(UUID b2bUnitId, PayrollPeriodStatus status) {
        List<PayrollPeriod> periods = (status != null)
                ? periodService.listByOrgAndStatus(b2bUnitId, status)
                : periodService.listByOrg(b2bUnitId);
        return periodConverter.convertAll(periods);
    }

    @Override
    public PayrollPeriodDto startProcessing(UUID periodId, UUID initiatedBy) {
        return periodConverter.convert(periodService.startProcessing(periodId, initiatedBy));
    }

    @Override
    public PayrollPeriodDto finalizePeriod(UUID periodId, UUID approvedBy, FinalizePayrollRequestDto request) {
        return periodConverter.convert(periodService.finalize(periodId, approvedBy));
    }

    @Override
    public PayrollPeriodDto markPaid(UUID periodId, MarkPaidRequestDto request) {
        return periodConverter.convert(
                periodService.markPaid(periodId, request.getPaymentDate(), request.getRemarks()));
    }

    // ── Salary Structures ──────────────────────────────────────────────────

    @Override
    public SalaryStructureDto createSalaryStructure(CreateSalaryStructureRequestDto request) {
        SalaryStructure structure = new SalaryStructure(
                request.getEmployeeId(), request.getName(), request.getEffectiveFrom());

        for (var c : request.getComponents()) {
            SalaryStructureComponent component = new SalaryStructureComponent();
            component.setSalaryStructure(structure);
            component.setComponentType(c.getComponentType());
            component.setName(c.getName());
            component.setCalculationType(c.getCalculationType());
            component.setValue(c.getValue());
            component.setTaxable(c.isTaxable());
            component.setEarning(c.isEarning());
            component.setSortOrder(c.getSortOrder());
            structure.getComponents().add(component);
        }

        return structureConverter.convert(structureService.create(structure));
    }

    @Override
    public SalaryStructureDto getActiveSalaryStructure(UUID employeeId) {
        return structureConverter.convert(structureService.getActive(employeeId));
    }

    @Override
    public List<SalaryStructureDto> listSalaryStructureHistory(UUID employeeId) {
        return structureConverter.convertAll(structureService.listHistory(employeeId));
    }

    // ── Payslips ───────────────────────────────────────────────────────────

    @Override
    public PayslipDto generatePayslip(UUID periodId, GeneratePayslipRequestDto request) {
        return payslipConverter.convert(
                payslipService.generate(periodId, request.getEmployeeId(), request.getLopDays()));
    }

    @Override
    public List<PayslipDto> bulkGeneratePayslips(UUID periodId, BulkGeneratePayslipRequestDto request) {
        return request.getEmployees().stream()
                .map(e -> payslipConverter.convert(
                        payslipService.generate(periodId, e.getEmployeeId(), e.getLopDays())))
                .toList();
    }

    @Override
    public PayslipDto getPayslip(UUID payslipId) {
        return payslipConverter.convert(payslipService.getById(payslipId));
    }

    @Override
    public List<PayslipDto> listPayslipsByPeriod(UUID periodId) {
        return payslipConverter.convertAll(payslipService.listByPeriod(periodId));
    }

    @Override
    public List<PayslipDto> listPayslipsByEmployee(UUID employeeId) {
        return payslipConverter.convertAll(payslipService.listByEmployee(employeeId));
    }

    @Override
    public PayslipDto getPayslipByEmployeeAndPeriod(UUID employeeId, int year, int month) {
        return payslipConverter.convert(payslipService.getByEmployeeAndPeriod(employeeId, year, month));
    }

    @Override
    public PayslipDto approvePayslip(UUID payslipId, UUID approvedBy) {
        return payslipConverter.convert(payslipService.approve(payslipId, approvedBy));
    }

    @Override
    public PayslipDto markPayslipPaid(UUID payslipId, MarkPayslipPaidRequestDto request) {
        return payslipConverter.convert(payslipService.markPaid(payslipId, request.getPaymentReference()));
    }

    @Override
    public PayslipDto revisePayslip(UUID payslipId, String reason) {
        return payslipConverter.convert(payslipService.revise(payslipId, reason));
    }

    // ── Tax Declarations ───────────────────────────────────────────────────

    @Override
    public TaxDeclarationDto submitTaxDeclaration(UUID employeeId, SubmitTaxDeclarationRequestDto request) {
        TaxDeclaration declaration = new TaxDeclaration();
        declaration.setEmployeeId(employeeId);
        declaration.setSection(request.getSection());
        declaration.setFinancialYear(request.getFinancialYear());
        declaration.setDescription(request.getDescription());
        declaration.setDeclaredAmount(request.getDeclaredAmount());
        declaration.setDocumentUrl(request.getDocumentUrl());
        return taxConverter.convert(taxService.submit(declaration));
    }

    @Override
    public List<TaxDeclarationDto> listTaxDeclarations(UUID employeeId, String financialYear) {
        List<TaxDeclaration> list = (financialYear != null && !financialYear.isBlank())
                ? taxService.listByEmployeeAndYear(employeeId, financialYear)
                : taxService.listByEmployee(employeeId);
        return taxConverter.convertAll(list);
    }

    @Override
    public List<TaxDeclarationDto> listPendingTaxDeclarations() {
        return taxConverter.convertAll(taxService.listPending());
    }

    @Override
    public TaxDeclarationDto reviewTaxDeclaration(UUID id, ReviewTaxDeclarationRequestDto request, UUID reviewedBy) {
        TaxDeclaration reviewed = switch (request.getDecision()) {
            case APPROVED -> taxService.approve(id, request.getApprovedAmount(), reviewedBy);
            case REJECTED -> taxService.reject(id, reviewedBy);
            default -> throw new IllegalArgumentException("Invalid decision: " + request.getDecision());
        };
        return taxConverter.convert(reviewed);
    }

    // ── Reimbursements ─────────────────────────────────────────────────────

    @Override
    public ReimbursementDto submitReimbursement(UUID employeeId, SubmitReimbursementRequestDto request) {
        Reimbursement r = new Reimbursement();
        r.setEmployeeId(employeeId);
        r.setCategory(request.getCategory());
        r.setDescription(request.getDescription());
        r.setClaimAmount(request.getClaimAmount());
        r.setClaimDate(request.getClaimDate());
        r.setReceiptUrl(request.getReceiptUrl());
        return reimbursementConverter.convert(reimbursementService.submit(r));
    }

    @Override
    public List<ReimbursementDto> listReimbursements(UUID employeeId, ReimbursementStatus status) {
        List<Reimbursement> list = (status != null)
                ? reimbursementService.listByEmployeeAndStatus(employeeId, status)
                : reimbursementService.listByEmployee(employeeId);
        return reimbursementConverter.convertAll(list);
    }

    @Override
    public List<ReimbursementDto> listPendingReimbursements() {
        return reimbursementConverter.convertAll(reimbursementService.listPending());
    }

    @Override
    public ReimbursementDto approveReimbursement(UUID id, ApproveReimbursementRequestDto request, UUID approvedBy) {
        return reimbursementConverter.convert(
                reimbursementService.approve(id, request.getApprovedAmount(), approvedBy));
    }

    @Override
    public ReimbursementDto rejectReimbursement(UUID id, RejectReimbursementRequestDto request, UUID rejectedBy) {
        return reimbursementConverter.convert(
                reimbursementService.reject(id, request.getRemarks(), rejectedBy));
    }

    @Override
    public List<ReimbursementDto> settleReimbursements(SettleReimbursementsRequestDto request) {
        return reimbursementConverter.convertAll(
                reimbursementService.settleInPeriod(request.getReimbursementIds(), request.getPeriodId()));
    }
}
