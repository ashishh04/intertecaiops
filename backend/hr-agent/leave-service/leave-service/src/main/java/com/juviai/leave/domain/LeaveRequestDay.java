package com.juviai.leave.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Day-level exploded breakdown of a leave request.
 * Weekends and public holidays are excluded — only actual working days appear here.
 *
 * Used for:
 *  - Precise LOP calculation per calendar day (fed to payroll-service)
 *  - Audit trail of exactly which days were consumed per request
 */
@Entity
@Table(
    name = "leave_request_days",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_lrd",
        columnNames = {"leave_request_id", "leave_date"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class LeaveRequestDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    /** 1.0 = full day, 0.5 = half day */
    @Column(name = "day_fraction", nullable = false, precision = 3, scale = 2)
    private BigDecimal dayFraction = BigDecimal.ONE;

    public LeaveRequestDay(LeaveRequest leaveRequest, LocalDate date, BigDecimal fraction) {
        this.leaveRequest = leaveRequest;
        this.leaveDate    = date;
        this.dayFraction  = fraction;
    }
}
