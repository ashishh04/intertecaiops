package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Annual tax-saving declaration submitted by an employee.
 * HR/Finance reviews and approves/rejects with an approved amount.
 * Approved declarations reduce the TDS computed each month.
 */
@Entity
@Table(name = "tax_declarations")
@Getter
@Setter
@NoArgsConstructor
public class TaxDeclaration extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "financial_year", nullable = false, length = 10)
    private String financialYear;   // e.g. "2025-26"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaxSection section;

    @Column(nullable = false, length = 256)
    private String description;

    @Column(name = "declared_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal declaredAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaxDeclarationStatus status = TaxDeclarationStatus.PENDING;

    @Column(name = "document_url", length = 512)
    private String documentUrl;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
