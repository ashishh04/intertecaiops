package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Immutable snapshot of a single earning/deduction line on a payslip.
 * Created during payroll generation; must not be modified after payslip status = APPROVED.
 */
@Entity
@Table(name = "payslip_components")
@Getter
@Setter
@NoArgsConstructor
public class PayslipComponent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payslip_id", nullable = false)
    private Payslip payslip;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 32)
    private ComponentType componentType;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * true = earning (shown under earnings on payslip).
     * false = deduction (shown under deductions).
     */
    @Column(name = "is_earning", nullable = false)
    private boolean earning = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public PayslipComponent(Payslip payslip, ComponentType type, String name,
                            BigDecimal amount, boolean earning, int sortOrder) {
        this.payslip = payslip;
        this.componentType = type;
        this.name = name;
        this.amount = amount;
        this.earning = earning;
        this.sortOrder = sortOrder;
    }
}
