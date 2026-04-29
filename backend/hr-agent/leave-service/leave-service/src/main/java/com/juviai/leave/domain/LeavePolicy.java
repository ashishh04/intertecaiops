package com.juviai.leave.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Defines annual allocation rules for a leave type.
 * E.g. CL = 12 days/year UPFRONT for ALL employees.
 *      ML = 26 weeks UPFRONT for GENDER_FEMALE employees.
 */
@Entity
@Table(name = "leave_policies")
@Getter @Setter @NoArgsConstructor
public class LeavePolicy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "b2b_unit_id", nullable = false)
    private UUID b2bUnitId;

    /** ALL | GENDER_FEMALE | GENDER_MALE | ROLE_SPECIFIC */
    @Column(name = "applicable_to", nullable = false, length = 32)
    private String applicableTo = "ALL";

    @Column(length = 16)
    private String gender;    // NULL = no gender filter

    @Column(name = "days_per_year", nullable = false, precision = 5, scale = 2)
    private BigDecimal daysPerYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "accrual_type", nullable = false, length = 32)
    private AccrualType accrualType = AccrualType.UPFRONT;

    /** Minimum employment tenure in days before this leave is available */
    @Column(name = "min_tenure_days", nullable = false)
    private int minTenureDays = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
