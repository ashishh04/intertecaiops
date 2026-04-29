package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An expense reimbursement claim submitted by an employee.
 * Flow: PENDING → APPROVED/REJECTED → PAID (settled in a payroll period).
 */
@Entity
@Table(name = "reimbursements")
@Getter
@Setter
@NoArgsConstructor
public class Reimbursement extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ReimbursementCategory category;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(name = "claim_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "receipt_url", length = 512)
    private String receiptUrl;

    @Column(name = "claim_date", nullable = false)
    private LocalDate claimDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReimbursementStatus status = ReimbursementStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_in_period")
    private PayrollPeriod paidInPeriod;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
