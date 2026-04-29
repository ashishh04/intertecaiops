package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single earning or deduction line within a salary structure template.
 * Example: BASIC FIXED 50000, HRA PERCENTAGE_OF_BASIC 40, PF PERCENTAGE_OF_BASIC 12.
 */
@Entity
@Table(name = "salary_structure_components")
@Getter
@Setter
@NoArgsConstructor
public class SalaryStructureComponent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 32)
    private ComponentType componentType;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 32)
    private CalculationType calculationType;

    /**
     * For FIXED: absolute amount in INR.
     * For PERCENTAGE_*: percentage value (e.g. 40.00 = 40%).
     */
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal value;

    @Column(name = "is_taxable", nullable = false)
    private boolean taxable = true;

    /**
     * true = earning (adds to gross), false = deduction (subtracted from gross).
     */
    @Column(name = "is_earning", nullable = false)
    private boolean earning = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
