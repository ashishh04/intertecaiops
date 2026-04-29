package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One payslip per employee per payroll period.
 * Components are snapshotted at generation time and become immutable after APPROVED.
 *
 * Net Pay = Gross Earnings − Total Deductions
 */
@Entity
@Table(
    name = "payslips",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_payslip",
        columnNames = {"payroll_period_id", "employee_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Payslip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_code", length = 64)
    private String employeeCode;

    @Column(name = "working_days", nullable = false)
    private int workingDays;

    @Column(name = "paid_days", nullable = false)
    private int paidDays;

    @Column(name = "lop_days", nullable = false)
    private int lopDays;   // Loss of Pay days

    @Column(name = "gross_earnings", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 15, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PayslipStatus status = PayslipStatus.DRAFT;

    @Column(name = "payment_reference", length = 128)
    private String paymentReference;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @OneToMany(mappedBy = "payslip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PayslipComponent> components = new ArrayList<>();

    /**
     * Recomputes grossEarnings, totalDeductions, and netPay from the current components list.
     * Call after adding/modifying components before persisting.
     */
    public void recomputeTotals() {
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        for (PayslipComponent c : components) {
            if (c.isEarning()) {
                earnings = earnings.add(c.getAmount());
            } else {
                deductions = deductions.add(c.getAmount());
            }
        }
        this.grossEarnings = earnings;
        this.totalDeductions = deductions;
        this.netPay = earnings.subtract(deductions);
    }
}
