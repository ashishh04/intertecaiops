package com.juviai.payroll.service.impl;

import com.juviai.payroll.domain.*;
import com.juviai.payroll.repo.PayrollPeriodRepository;
import com.juviai.payroll.repo.PayslipRepository;
import com.juviai.payroll.service.PayslipService;
import com.juviai.payroll.service.SalaryStructureService;
import com.juviai.payroll.service.TaxDeclarationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayslipServiceImpl implements PayslipService {

    private static final BigDecimal PF_WAGE_CEILING    = new BigDecimal("15000");
    private static final BigDecimal PF_EMPLOYER_RATE   = new BigDecimal("12");   // 12%
    private static final BigDecimal PROFESSIONAL_TAX   = new BigDecimal("200");  // ₹200/month flat (Karnataka)
    private static final int        TDS_SLABS_DIVISOR  = 12;                     // annualise → monthly

    private final PayslipRepository           payslipRepository;
    private final PayrollPeriodRepository     periodRepository;
    private final SalaryStructureService      salaryStructureService;
    private final TaxDeclarationService       taxDeclarationService;

    // ── Read operations ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Payslip getById(UUID id) {
        return payslipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payslip not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payslip> listByPeriod(UUID periodId) {
        return payslipRepository.findByPayrollPeriodIdOrderByEmployeeId(periodId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payslip> listByEmployee(UUID employeeId) {
        return payslipRepository
                .findByEmployeeIdOrderByPayrollPeriodPeriodYearDescPayrollPeriodPeriodMonthDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Payslip getByEmployeeAndPeriod(UUID employeeId, int year, int month) {
        return payslipRepository.findByEmployeeAndPeriod(employeeId, year, month)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payslip not found for employee " + employeeId + " — " + year + "/" + month));
    }

    // ── Payslip generation ───────────────────────────────────────────────────

    /**
     * Core payroll computation engine.
     *
     * Algorithm:
     *  1. Resolve salary structure → get all component templates.
     *  2. Compute prorated basic (if LOP days > 0):  basic × (paidDays / workingDays).
     *  3. Compute all earnings in declaration order (BASIC first, then PERCENTAGE_OF_BASIC, then rest).
     *  4. Compute statutory deductions: PF (capped at ₹15,000 wage ceiling), Professional Tax, TDS.
     *  5. Snapshot components onto payslip.
     *  6. Recompute totals (gross − deductions = net pay).
     */
    @Override
    @Transactional
    public Payslip generate(UUID periodId, UUID employeeId, int lopDays) {
        PayrollPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found: " + periodId));

        if (period.getStatus() == PayrollPeriodStatus.FINALIZED
                || period.getStatus() == PayrollPeriodStatus.PAID) {
            throw new IllegalStateException(
                    "Cannot generate payslip: period is already " + period.getStatus());
        }

        // Idempotent — delete existing DRAFT payslip if regenerating
        Optional<Payslip> existing = payslipRepository.findByPayrollPeriodIdAndEmployeeId(periodId, employeeId);
        existing.ifPresent(p -> {
            if (p.getStatus() != PayslipStatus.DRAFT) {
                throw new IllegalStateException(
                        "Payslip is already " + p.getStatus() + " — cannot regenerate");
            }
            payslipRepository.delete(p);
            payslipRepository.flush();
        });

        SalaryStructure structure = salaryStructureService.getActive(employeeId);

        // Working days in the period month
        int workingDays = YearMonth.of(period.getPeriodYear(), period.getPeriodMonth()).lengthOfMonth();
        int paidDays    = Math.max(0, workingDays - lopDays);

        Payslip payslip = new Payslip();
        payslip.setPayrollPeriod(period);
        payslip.setEmployeeId(employeeId);
        payslip.setWorkingDays(workingDays);
        payslip.setPaidDays(paidDays);
        payslip.setLopDays(lopDays);
        payslip.setStatus(PayslipStatus.DRAFT);
        payslip.setGeneratedAt(Instant.now());

        // ── Step 1: resolve BASIC (always first, others depend on it) ────────
        BigDecimal basicFull = resolveFixed(structure, ComponentType.BASIC);
        BigDecimal basic     = prorate(basicFull, paidDays, workingDays);

        // ── Step 2: compute all earnings ─────────────────────────────────────
        int order = 0;
        payslip.getComponents().add(new PayslipComponent(
                payslip, ComponentType.BASIC, "Basic Salary", basic, true, order++));

        for (SalaryStructureComponent template : structure.getComponents()) {
            if (template.getComponentType() == ComponentType.BASIC) continue;      // already done
            if (!template.isEarning()) continue;                                   // deductions later

            BigDecimal amount = computeAmount(template, basic, payslip, workingDays, paidDays);
            payslip.getComponents().add(new PayslipComponent(
                    payslip, template.getComponentType(), template.getName(), amount, true, order++));
        }

        // ── Step 3: gross earnings snapshot ──────────────────────────────────
        BigDecimal gross = payslip.getComponents().stream()
                .filter(PayslipComponent::isEarning)
                .map(PayslipComponent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Step 4: statutory deductions ─────────────────────────────────────

        // PF: 12% of basic, capped at 12% of ₹15,000 = ₹1,800
        BigDecimal pfWage  = basic.min(PF_WAGE_CEILING);
        BigDecimal pfAmt   = pfWage.multiply(PF_EMPLOYER_RATE)
                                   .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        payslip.getComponents().add(new PayslipComponent(
                payslip, ComponentType.PF, "Provident Fund (Employee)", pfAmt, false, order++));

        // Professional Tax (flat, only if not LOP full month)
        if (paidDays > 0) {
            payslip.getComponents().add(new PayslipComponent(
                    payslip, ComponentType.PROFESSIONAL_TAX, "Professional Tax",
                    PROFESSIONAL_TAX, false, order++));
        }

        // TDS: annualise gross → apply new-tax-regime slabs → monthly TDS
        // Reduce by approved tax declarations
        String financialYear = toFinancialYear(period.getPeriodYear(), period.getPeriodMonth());
        BigDecimal approvedDeclarations = taxDeclarationService.totalApprovedAmount(employeeId, financialYear);
        BigDecimal annualGross    = gross.multiply(BigDecimal.valueOf(12));
        BigDecimal taxableIncome  = annualGross.subtract(approvedDeclarations).max(BigDecimal.ZERO);
        BigDecimal annualTax      = computeIncomeTax(taxableIncome);
        BigDecimal monthlyTds     = annualTax.divide(BigDecimal.valueOf(TDS_SLABS_DIVISOR), 2, RoundingMode.HALF_UP);

        if (monthlyTds.compareTo(BigDecimal.ZERO) > 0) {
            payslip.getComponents().add(new PayslipComponent(
                    payslip, ComponentType.TDS, "Tax Deducted at Source (TDS)", monthlyTds, false, order++));
        }

        // Any custom deduction components from salary structure
        for (SalaryStructureComponent template : structure.getComponents()) {
            if (template.isEarning()) continue;
            if (template.getComponentType() == ComponentType.PF
                    || template.getComponentType() == ComponentType.PROFESSIONAL_TAX
                    || template.getComponentType() == ComponentType.TDS) continue; // already computed

            BigDecimal amount = computeAmount(template, basic, payslip, workingDays, paidDays);
            payslip.getComponents().add(new PayslipComponent(
                    payslip, template.getComponentType(), template.getName(), amount, false, order++));
        }

        payslip.recomputeTotals();
        Payslip saved = payslipRepository.save(payslip);
        log.info("Generated payslip {} for employee {} in period {} — net pay: {}",
                saved.getId(), employeeId, periodId, saved.getNetPay());
        return saved;
    }

    @Override
    @Transactional
    public Payslip approve(UUID payslipId, UUID approvedBy) {
        Payslip payslip = getById(payslipId);
        if (payslip.getStatus() != PayslipStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT payslips can be approved. Current: " + payslip.getStatus());
        }
        payslip.setStatus(PayslipStatus.APPROVED);
        log.info("Payslip {} approved by {}", payslipId, approvedBy);
        return payslipRepository.save(payslip);
    }

    @Override
    @Transactional
    public Payslip markPaid(UUID payslipId, String paymentReference) {
        Payslip payslip = getById(payslipId);
        if (payslip.getStatus() != PayslipStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED payslips can be marked PAID. Current: " + payslip.getStatus());
        }
        payslip.setStatus(PayslipStatus.PAID);
        payslip.setPaymentReference(paymentReference);
        payslip.setPaidAt(Instant.now());
        log.info("Payslip {} marked PAID, reference: {}", payslipId, paymentReference);
        return payslipRepository.save(payslip);
    }

    @Override
    @Transactional
    public Payslip revise(UUID payslipId, String reason) {
        Payslip original = getById(payslipId);
        if (original.getStatus() != PayslipStatus.PAID) {
            throw new IllegalStateException("Only PAID payslips can be revised.");
        }
        original.setStatus(PayslipStatus.REVISED);
        payslipRepository.save(original);

        // Clone into a new DRAFT payslip for correction
        Payslip revised = new Payslip();
        revised.setPayrollPeriod(original.getPayrollPeriod());
        revised.setEmployeeId(original.getEmployeeId());
        revised.setEmployeeCode(original.getEmployeeCode());
        revised.setWorkingDays(original.getWorkingDays());
        revised.setPaidDays(original.getPaidDays());
        revised.setLopDays(original.getLopDays());
        revised.setGrossEarnings(original.getGrossEarnings());
        revised.setTotalDeductions(original.getTotalDeductions());
        revised.setNetPay(original.getNetPay());
        revised.setStatus(PayslipStatus.DRAFT);
        revised.setGeneratedAt(Instant.now());

        for (PayslipComponent c : original.getComponents()) {
            revised.getComponents().add(new PayslipComponent(
                    revised, c.getComponentType(), c.getName(),
                    c.getAmount(), c.isEarning(), c.getSortOrder()));
        }

        Payslip saved = payslipRepository.save(revised);
        log.info("Payslip {} revised → new DRAFT payslip {}, reason: {}", payslipId, saved.getId(), reason);
        return saved;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private BigDecimal resolveFixed(SalaryStructure structure, ComponentType type) {
        return structure.getComponents().stream()
                .filter(c -> c.getComponentType() == type && c.getCalculationType() == CalculationType.FIXED)
                .map(SalaryStructureComponent::getValue)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal prorate(BigDecimal amount, int paidDays, int workingDays) {
        if (workingDays == 0) return BigDecimal.ZERO;
        if (paidDays == workingDays) return amount;
        return amount.multiply(BigDecimal.valueOf(paidDays))
                     .divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeAmount(SalaryStructureComponent template, BigDecimal basic,
                                     Payslip payslip, int workingDays, int paidDays) {
        BigDecimal raw;
        switch (template.getCalculationType()) {
            case FIXED -> raw = template.getValue();
            case PERCENTAGE_OF_BASIC -> raw = basic.multiply(template.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case PERCENTAGE_OF_GROSS -> {
                BigDecimal currentGross = payslip.getComponents().stream()
                        .filter(PayslipComponent::isEarning)
                        .map(PayslipComponent::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                raw = currentGross.multiply(template.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            default -> raw = BigDecimal.ZERO;
        }
        return prorate(raw, paidDays, workingDays);
    }

    /**
     * Income tax computation — New Tax Regime FY 2025-26 (Budget 2024 slabs).
     * Taxable income → annual tax → caller divides by 12 for monthly TDS.
     *
     * Slabs (New Regime):
     *  ≤ 3,00,000        → NIL
     *  3,00,001–7,00,000 → 5%
     *  7,00,001–10,00,000→ 10%
     * 10,00,001–12,00,000→ 15%
     * 12,00,001–15,00,000→ 20%
     * > 15,00,000        → 30%
     * + 4% Health & Education Cess on tax
     */
    private BigDecimal computeIncomeTax(BigDecimal taxableIncome) {
        BigDecimal tax = BigDecimal.ZERO;

        BigDecimal slab1End = new BigDecimal("300000");
        BigDecimal slab2End = new BigDecimal("700000");
        BigDecimal slab3End = new BigDecimal("1000000");
        BigDecimal slab4End = new BigDecimal("1200000");
        BigDecimal slab5End = new BigDecimal("1500000");

        if (taxableIncome.compareTo(slab1End) <= 0) {
            return BigDecimal.ZERO;
        }

        if (taxableIncome.compareTo(slab2End) <= 0) {
            tax = taxableIncome.subtract(slab1End).multiply(new BigDecimal("0.05"));
        } else if (taxableIncome.compareTo(slab3End) <= 0) {
            tax = slab2End.subtract(slab1End).multiply(new BigDecimal("0.05"))
                          .add(taxableIncome.subtract(slab2End).multiply(new BigDecimal("0.10")));
        } else if (taxableIncome.compareTo(slab4End) <= 0) {
            tax = slab2End.subtract(slab1End).multiply(new BigDecimal("0.05"))
                          .add(slab3End.subtract(slab2End).multiply(new BigDecimal("0.10")))
                          .add(taxableIncome.subtract(slab3End).multiply(new BigDecimal("0.15")));
        } else if (taxableIncome.compareTo(slab5End) <= 0) {
            tax = slab2End.subtract(slab1End).multiply(new BigDecimal("0.05"))
                          .add(slab3End.subtract(slab2End).multiply(new BigDecimal("0.10")))
                          .add(slab4End.subtract(slab3End).multiply(new BigDecimal("0.15")))
                          .add(taxableIncome.subtract(slab4End).multiply(new BigDecimal("0.20")));
        } else {
            tax = slab2End.subtract(slab1End).multiply(new BigDecimal("0.05"))
                          .add(slab3End.subtract(slab2End).multiply(new BigDecimal("0.10")))
                          .add(slab4End.subtract(slab3End).multiply(new BigDecimal("0.15")))
                          .add(slab5End.subtract(slab4End).multiply(new BigDecimal("0.20")))
                          .add(taxableIncome.subtract(slab5End).multiply(new BigDecimal("0.30")));
        }

        // 4% Health & Education Cess
        tax = tax.multiply(new BigDecimal("1.04")).setScale(2, RoundingMode.HALF_UP);
        return tax;
    }

    private String toFinancialYear(int periodYear, int periodMonth) {
        // April (4) starts new FY in India
        if (periodMonth >= 4) {
            return periodYear + "-" + String.valueOf(periodYear + 1).substring(2);
        } else {
            return (periodYear - 1) + "-" + String.valueOf(periodYear).substring(2);
        }
    }
}
