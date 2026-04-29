package com.juviai.leave.repo;

import com.juviai.leave.domain.LeaveRequest;
import com.juviai.leave.domain.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(UUID employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatusOrderByAppliedAtDesc(
            UUID employeeId, LeaveRequestStatus status);

    List<LeaveRequest> findByStatusOrderByAppliedAtAsc(LeaveRequestStatus status);

    /** All pending leave requests within a manager's org unit */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.status = 'PENDING'
          AND lr.leaveType.b2bUnitId = :b2bUnitId
        ORDER BY lr.appliedAt ASC
    """)
    List<LeaveRequest> findPendingByOrg(@Param("b2bUnitId") UUID b2bUnitId);

    /** Check for overlapping approved/pending leave requests for an employee */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employeeId = :employeeId
          AND lr.status IN ('PENDING', 'APPROVED')
          AND lr.fromDate <= :toDate
          AND lr.toDate >= :fromDate
    """)
    List<LeaveRequest> findOverlapping(
            @Param("employeeId") UUID employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /**
     * Fetch all approved leave requests for LOP calculation in a payroll period.
     * Used by payroll-service to compute LOP days per employee.
     */
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employeeId = :employeeId
          AND lr.status = 'APPROVED'
          AND lr.leaveType.paid = false
          AND lr.fromDate <= :periodEnd
          AND lr.toDate >= :periodStart
    """)
    List<LeaveRequest> findLopRequestsInPeriod(
            @Param("employeeId") UUID employeeId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
