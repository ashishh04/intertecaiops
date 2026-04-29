package com.juviai.payroll.repo;

import com.juviai.payroll.domain.Reimbursement;
import com.juviai.payroll.domain.ReimbursementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReimbursementRepository extends JpaRepository<Reimbursement, UUID> {

    List<Reimbursement> findByEmployeeIdOrderByClaimDateDesc(UUID employeeId);

    List<Reimbursement> findByEmployeeIdAndStatusOrderByClaimDateDesc(UUID employeeId, ReimbursementStatus status);

    List<Reimbursement> findByStatusOrderByCreatedAtAsc(ReimbursementStatus status);

    List<Reimbursement> findByStatusAndPaidInPeriodIsNull(ReimbursementStatus status);
}
