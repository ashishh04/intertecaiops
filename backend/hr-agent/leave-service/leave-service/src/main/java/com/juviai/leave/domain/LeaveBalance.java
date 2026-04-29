package com.juviai.leave.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tracks the leave balance for a single employee per leave type per year.
 *
 * available = allocated + carried_forward - used - pending
 * lop_days  = days taken as Loss of Pay (no balance available)
 */
@Entity
@Table(
    name = "leave_balances",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_leave_balance",
        columnNames = {"employee_id", "leave_type_id", "year"}
    )
)
@Getter @Setter @NoArgsConstructor
public class LeaveBalance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(name = "allocated_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal allocatedDays = BigDecimal.ZERO;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal usedDays = BigDecimal.ZERO;

    /** Days in PENDING approval — reserved but not yet consumed */
    @Column(name = "pending_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal pendingDays = BigDecimal.ZERO;

    @Column(name = "carried_forward", nullable = false, precision = 5, scale = 2)
    private BigDecimal carriedForward = BigDecimal.ZERO;

    @Column(name = "lop_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal lopDays = BigDecimal.ZERO;

    public BigDecimal getAvailableDays() {
        return allocatedDays
                .add(carriedForward)
                .subtract(usedDays)
                .subtract(pendingDays)
                .max(BigDecimal.ZERO);
    }

    public LeaveBalance(UUID employeeId, LeaveType leaveType, int year, BigDecimal allocatedDays) {
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.year = year;
        this.allocatedDays = allocatedDays;
    }
}
