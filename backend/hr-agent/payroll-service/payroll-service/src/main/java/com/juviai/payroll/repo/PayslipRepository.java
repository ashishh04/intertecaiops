package com.juviai.payroll.repo;

import com.juviai.payroll.domain.Payslip;
import com.juviai.payroll.domain.PayslipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {

    Optional<Payslip> findByPayrollPeriodIdAndEmployeeId(UUID periodId, UUID employeeId);

    List<Payslip> findByPayrollPeriodIdOrderByEmployeeId(UUID periodId);

    List<Payslip> findByEmployeeIdOrderByPayrollPeriodPeriodYearDescPayrollPeriodPeriodMonthDesc(
            UUID employeeId);

    List<Payslip> findByPayrollPeriodIdAndStatus(UUID periodId, PayslipStatus status);

    boolean existsByPayrollPeriodIdAndEmployeeId(UUID periodId, UUID employeeId);

    @Query("""
        SELECT p FROM Payslip p
        WHERE p.employeeId = :employeeId
          AND p.payrollPeriod.periodYear = :year
          AND p.payrollPeriod.periodMonth = :month
    """)
    Optional<Payslip> findByEmployeeAndPeriod(
            @Param("employeeId") UUID employeeId,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
        SELECT COUNT(p) FROM Payslip p
        WHERE p.payrollPeriod.id = :periodId
          AND p.status = 'APPROVED'
    """)
    long countApprovedByPeriod(@Param("periodId") UUID periodId);
}
