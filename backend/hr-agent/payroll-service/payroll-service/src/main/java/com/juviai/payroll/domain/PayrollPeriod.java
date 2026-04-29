package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single payroll cycle (e.g. April 2026) for a given org unit.
 * Status flows: DRAFT → PROCESSING → FINALIZED → PAID.
 * Once FINALIZED, payslips are locked and cannot be edited.
 */
@Entity
@Table(
    name = "payroll_periods",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_payroll_period",
        columnNames = {"b2b_unit_id", "period_year", "period_month"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class PayrollPeriod extends BaseEntity {

    @Column(name = "b2b_unit_id", nullable = false)
    private UUID b2bUnitId;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;   // 1–12

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PayrollPeriodStatus status = PayrollPeriodStatus.DRAFT;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;


    @OneToMany(mappedBy = "payrollPeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payslip> payslips = new ArrayList<>();

    public PayrollPeriod(UUID b2bUnitId, int year, int month, UUID createdBy) {
        this.b2bUnitId = b2bUnitId;
        this.periodYear = year;
        this.periodMonth = month;
        this.setCreatedBy(createdBy != null ? createdBy.toString() : null);
        this.status = PayrollPeriodStatus.DRAFT;
    }
}
