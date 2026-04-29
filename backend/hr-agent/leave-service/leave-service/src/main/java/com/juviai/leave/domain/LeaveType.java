package com.juviai.leave.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Master configuration for a leave type within an org unit.
 * Examples: Casual Leave (CL), Sick Leave (SL), Earned Leave (EL),
 *           Maternity Leave (ML), Paternity Leave (PL), Comp Off.
 */
@Entity
@Table(
    name = "leave_types",
    uniqueConstraints = @UniqueConstraint(name = "uq_leave_type", columnNames = {"b2b_unit_id", "code"})
)
@Getter
@Setter
@NoArgsConstructor
public class LeaveType extends BaseEntity {

    @Column(name = "b2b_unit_id", nullable = false)
    private UUID b2bUnitId;

    /** Short code used in payslip LOP calculation — e.g. CL, SL, EL, ML */
    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Whether this leave is paid. Loss-of-Pay = !isPaid */
    @Column(name = "is_paid", nullable = false)
    private boolean paid = true;

    /** Whether employee must upload a document (e.g. medical cert for SL > 2 days) */
    @Column(name = "requires_document", nullable = false)
    private boolean requiresDocument = false;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "carry_forward_allowed", nullable = false)
    private boolean carryForwardAllowed = false;

    @Column(name = "max_carry_forward_days", nullable = false)
    private int maxCarryForwardDays = 0;

    /** Whether un-used leave can be encashed at year end */
    @Column(nullable = false)
    private boolean encashable = false;

    @Column(nullable = false)
    private boolean active = true;
}
