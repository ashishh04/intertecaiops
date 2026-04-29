package com.juviai.user.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "employee_code_sequence",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_code_seq_b2b", columnNames = {"b2b_unit_id"})
        },
        indexes = {
                @Index(name = "idx_employee_code_seq_b2b", columnList = "b2b_unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EmployeeCodeSequence extends BaseEntity {

    @Column(name = "b2b_unit_id", nullable = false)
    private UUID b2bUnitId;

    @Column(name = "company_code", length = 64)
    private String companyCode;

    @Column(name = "next_value", nullable = false)
    private long nextValue = 1L;
}
