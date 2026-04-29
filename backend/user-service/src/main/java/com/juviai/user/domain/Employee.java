package com.juviai.user.domain;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.juviai.user.organisation.domain.Department;
import com.juviai.common.crypto.EncryptedStringConverter;
import com.juviai.common.crypto.EncryptedBigDecimalConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("EMPLOYEE")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends User {

    // In SINGLE_TABLE inheritance this column exists for all rows, so it must be nullable
    // to allow base User inserts where employeeCode is not applicable.
    @Column(nullable = true, unique = true, length = 64)
    private String employeeCode;

    @Column(name = "dismissed")
    private Boolean dismissed = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "designation_id")
    @JsonBackReference
    private Designation designation;

    @OneToOne
    @JoinColumn(name = "department_id", unique = true)
    @JsonIgnoreProperties({"b2bUnits"})
    private Department department;

    @jakarta.persistence.OneToOne
    @JoinColumn(name = "band_id")
    private EmployeeOrgBand band;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private EmploymentType employmentType;

    @ManyToOne
    @JoinColumn(name = "reporting_manager_id")
    private User reportingManager;

    /**
     * Stored encrypted — annual salary is high-sensitivity financial PII.
     * DB column must be VARCHAR(512) — change via migration if previously DOUBLE/DECIMAL.
     * SQL-level ORDER BY / aggregations are not possible; use application-layer sorting.
     */
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "annual_salary", length = 512)
    private BigDecimal annualSalary;

    @Column(name = "store_id", nullable = true)
    private UUID storeId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "pf_number", length = 512)
    private String pfNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "uan_number", length = 512)
    private String uanNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "pan_number", length = 512)
    private String panNumber;

    @Column(name = "pf_enabled")
    private Boolean pfEnabled;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Compensation> compensations;

    // Constructor with all fields
    public Employee(String username, String email, String passwordHash, String firstName, String lastName, 
                   boolean active, String employeeCode, String designation, Department department, 
                   LocalDate hireDate, EmploymentType employmentType) {
        super(username, email, passwordHash, firstName, lastName, active);
        this.employeeCode = employeeCode;
        this.department = department;
        this.hireDate = hireDate;
        this.employmentType = employmentType;
    }
}
