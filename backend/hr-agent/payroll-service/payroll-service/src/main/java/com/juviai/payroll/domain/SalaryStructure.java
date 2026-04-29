package com.juviai.payroll.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Defines how an employee's salary is split into components.
 * Multiple structures can exist per employee but only one is active at a time.
 * When a salary revision happens: deactivate current + create new structure.
 */
@Entity
@Table(name = "salary_structures")
@Getter
@Setter
@NoArgsConstructor
public class SalaryStructure extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;   // NULL = currently active

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "salaryStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<SalaryStructureComponent> components = new ArrayList<>();

    public SalaryStructure(UUID employeeId, String name, LocalDate effectiveFrom) {
        this.employeeId = employeeId;
        this.name = name;
        this.effectiveFrom = effectiveFrom;
        this.active = true;
    }
}
