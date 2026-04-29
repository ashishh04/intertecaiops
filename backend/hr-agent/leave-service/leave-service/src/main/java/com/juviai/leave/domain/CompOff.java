package com.juviai.leave.domain;

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
 * Compensatory off credit earned when an employee works on a weekend or public holiday.
 * Flow: PENDING → APPROVED (credits usable as leave) → CONSUMED / EXPIRED.
 * Credits expire after a configured period (default 90 days from approval).
 */
@Entity
@Table(name = "comp_offs")
@Getter
@Setter
@NoArgsConstructor
public class CompOff extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "worked_date", nullable = false)
    private LocalDate workedDate;

    @Column(nullable = false, length = 256)
    private String reason;

    /** 0.5 = half-day worked, 1.0 = full day worked */
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal credits = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CompOffStatus status = CompOffStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    /** Comp-offs typically expire 90 days after approval */
    @Column(name = "expires_at")
    private LocalDate expiresAt;
}
