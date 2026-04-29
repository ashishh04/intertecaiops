package com.juviai.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.juviai.common.orm.BaseEntity;
import com.juviai.common.crypto.EncryptedStringConverter;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.domain.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "experiences")
@Getter
@Setter
@NoArgsConstructor
public class Experience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2b_unit_id")
    private B2BUnit b2bUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"b2bUnits"})
    private Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "start_month")
    private Integer startMonth;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(name = "end_month")
    private Integer endMonth;

    // Stored encrypted — job role title is personal career data (PII)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "role", length = 512)
    private String role;
}
