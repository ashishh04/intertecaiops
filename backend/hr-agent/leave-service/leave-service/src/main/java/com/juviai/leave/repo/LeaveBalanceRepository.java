package com.juviai.leave.repo;

import com.juviai.leave.domain.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    List<LeaveBalance> findByEmployeeIdAndYear(UUID employeeId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            UUID employeeId, UUID leaveTypeId, int year);

    List<LeaveBalance> findByEmployeeId(UUID employeeId);

    @Query("""
        SELECT lb FROM LeaveBalance lb
        WHERE lb.employeeId = :employeeId
          AND lb.year = :year
          AND lb.leaveType.b2bUnitId = :b2bUnitId
    """)
    List<LeaveBalance> findByEmployeeAndYearAndOrg(
            @Param("employeeId") UUID employeeId,
            @Param("year") int year,
            @Param("b2bUnitId") UUID b2bUnitId);
}
