package com.juviai.payroll.repo;

import com.juviai.payroll.domain.TaxDeclaration;
import com.juviai.payroll.domain.TaxDeclarationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TaxDeclarationRepository extends JpaRepository<TaxDeclaration, UUID> {

    List<TaxDeclaration> findByEmployeeIdAndFinancialYear(UUID employeeId, String financialYear);

    List<TaxDeclaration> findByEmployeeIdOrderByFinancialYearDescCreatedAtDesc(UUID employeeId);

    List<TaxDeclaration> findByStatusOrderByCreatedAtAsc(TaxDeclarationStatus status);

    boolean existsByEmployeeIdAndFinancialYearAndSection(
            UUID employeeId, String financialYear, com.juviai.payroll.domain.TaxSection section);
}
