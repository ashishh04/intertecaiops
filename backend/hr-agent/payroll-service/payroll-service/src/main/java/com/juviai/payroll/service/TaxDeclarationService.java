package com.juviai.payroll.service;

import com.juviai.payroll.domain.TaxDeclaration;
import com.juviai.payroll.domain.TaxDeclarationStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TaxDeclarationService {

    TaxDeclaration submit(TaxDeclaration declaration);

    TaxDeclaration getById(UUID id);

    List<TaxDeclaration> listByEmployee(UUID employeeId);

    List<TaxDeclaration> listByEmployeeAndYear(UUID employeeId, String financialYear);

    List<TaxDeclaration> listPending();

    TaxDeclaration approve(UUID id, BigDecimal approvedAmount, UUID reviewedBy);

    TaxDeclaration reject(UUID id, UUID reviewedBy);

    /**
     * Total approved deductions for an employee in a financial year.
     * Used by payslip generation to compute TDS.
     */
    BigDecimal totalApprovedAmount(UUID employeeId, String financialYear);
}
