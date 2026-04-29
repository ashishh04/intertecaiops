package com.juviai.payroll.dto;

import com.juviai.payroll.domain.*;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PayrollDtos {

// ── Payroll Period ────────────────────────────────────────────────────────────

@Data
public static class CreatePayrollPeriodRequestDto {
    @NotNull
    private UUID b2bUnitId;
    @NotNull @Min(2000) @Max(2100)
    private Integer year;
    @NotNull @Min(1) @Max(12)
    private Integer month;
}

@Data
public static class PayrollPeriodDto {
    private UUID id;
    private UUID b2bUnitId;
    private int periodYear;
    private int periodMonth;
    private String periodLabel;         // e.g. "April 2026"
    private PayrollPeriodStatus status;
    private LocalDate paymentDate;
    private String remarks;
    private int totalPayslips;
    private int approvedPayslips;
}

@Data
public static class FinalizePayrollRequestDto {
    @NotBlank
    private String remarks;
}

@Data
public static class MarkPaidRequestDto {
    @NotNull
    private LocalDate paymentDate;
    private String remarks;
}

// ── Salary Structure ──────────────────────────────────────────────────────────

@Data
public static class CreateSalaryStructureRequestDto {
    @NotNull
    private UUID employeeId;
    @NotBlank
    private String name;
    @NotNull
    private LocalDate effectiveFrom;
    @NotEmpty
    private List<SalaryStructureComponentDto> components;
}

@Data
public static class SalaryStructureComponentDto {
    private UUID id;
    @NotNull
    private ComponentType componentType;
    @NotBlank
    private String name;
    @NotNull
    private CalculationType calculationType;
    @NotNull @DecimalMin("0.0001")
    private BigDecimal value;
    private boolean taxable   = true;
    private boolean earning   = true;
    private int sortOrder     = 0;
}

@Data
public static class SalaryStructureDto {
    private UUID id;
    private UUID employeeId;
    private String name;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean active;
    private List<SalaryStructureComponentDto> components;
}

// ── Payslip ───────────────────────────────────────────────────────────────────

@Data
public static class GeneratePayslipRequestDto {
    @NotNull
    private UUID employeeId;
    @Min(0)
    private int lopDays = 0;
}

@Data
public static class BulkGeneratePayslipRequestDto {
    @NotEmpty
    private List<GeneratePayslipRequestDto> employees;
}

@Data
public static class PayslipComponentDto {
    private ComponentType componentType;
    private String name;
    private BigDecimal amount;
    private boolean earning;
    private int sortOrder;
}

@Data
public static class PayslipDto {
    private UUID id;
    private UUID payrollPeriodId;
    private String periodLabel;
    private UUID employeeId;
    private String employeeCode;
    private int workingDays;
    private int paidDays;
    private int lopDays;
    private BigDecimal grossEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private PayslipStatus status;
    private String paymentReference;
    private List<PayslipComponentDto> earnings;
    private List<PayslipComponentDto> deductions;
}

@Data
public static class ApprovePayslipRequestDto {
    // Intentionally empty — approval is an action, no body required
}

@Data
public static class MarkPayslipPaidRequestDto {
    @NotBlank
    private String paymentReference;
}

// ── Tax Declaration ───────────────────────────────────────────────────────────

@Data
public static class SubmitTaxDeclarationRequestDto {
    @NotNull
    private TaxSection section;
    @NotBlank
    private String financialYear;
    @NotBlank
    private String description;
    @NotNull @DecimalMin("1.00")
    private BigDecimal declaredAmount;
    private String documentUrl;
}

@Data
public static class ReviewTaxDeclarationRequestDto {
    @NotNull
    private TaxDeclarationStatus decision;   // APPROVED or REJECTED
    private BigDecimal approvedAmount;        // required if decision = APPROVED
}

@Data
public static class TaxDeclarationDto {
    private UUID id;
    private UUID employeeId;
    private String financialYear;
    private TaxSection section;
    private String description;
    private BigDecimal declaredAmount;
    private BigDecimal approvedAmount;
    private TaxDeclarationStatus status;
    private String documentUrl;
}

// ── Reimbursement ─────────────────────────────────────────────────────────────

@Data
public static class SubmitReimbursementRequestDto {
    @NotNull
    private ReimbursementCategory category;
    @NotBlank
    private String description;
    @NotNull @DecimalMin("1.00")
    private BigDecimal claimAmount;
    @NotNull
    private LocalDate claimDate;
    private String receiptUrl;
}

@Data
public static class ApproveReimbursementRequestDto {
    @NotNull @DecimalMin("0.00")
    private BigDecimal approvedAmount;
}

@Data
public static class RejectReimbursementRequestDto {
    @NotBlank
    private String remarks;
}

@Data
public static class SettleReimbursementsRequestDto {
    @NotEmpty
    private List<UUID> reimbursementIds;
    @NotNull
    private UUID periodId;
}

@Data
public static class ReimbursementDto {
    private UUID id;
    private UUID employeeId;
    private ReimbursementCategory category;
    private String description;
    private BigDecimal claimAmount;
    private BigDecimal approvedAmount;
    private LocalDate claimDate;
    private ReimbursementStatus status;
    private String receiptUrl;
    private String remarks;
}
}

