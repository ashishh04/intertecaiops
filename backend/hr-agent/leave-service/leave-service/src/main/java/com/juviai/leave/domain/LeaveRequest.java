package com.juviai.leave.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A leave application submitted by an employee.
 *
 * Status flow:
 *   PENDING → APPROVED  (manager/HR approves)
 *   PENDING → REJECTED  (manager/HR rejects)
 *   APPROVED → CANCELLED (employee cancels before start date)
 *   APPROVED → REVOKED  (HR revokes after approval, e.g. emergency)
 */
@Entity
@Table(name = "leave_requests")
@Getter @Setter @NoArgsConstructor
public class LeaveRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    /** Actual working days (excluding weekends and public holidays) */
    @Column(name = "total_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalDays;

    @Column(name = "half_day", nullable = false)
    private boolean halfDay = false;

    /** MORNING or AFTERNOON (only when halfDay = true) */
    @Column(name = "half_day_period", length = 8)
    private String halfDayPeriod;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "document_url", length = 512)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveRequestDay> days = new ArrayList<>();
}
