package com.juviai.user.dto;

import com.juviai.user.domain.EmploymentType;
import com.juviai.user.domain.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EmployeeData {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private UUID storeId;
    private String employeeCode;
    private Boolean dismissed;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String designation;
    private UUID designationId;
    private String department;
    private UUID departmentId;
    private String band;
    private UUID bandId;
    private LocalDate hireDate;
    private EmploymentType employmentType;
    private UUID reportingManagerId;
    private String reportingManagerName;
    private Instant createdDate;
    private Instant updatedDate;
    private BigDecimal annualSalary;
    private String pfNumber;
    private String uanNumber;
    private String panNumber;
    private Boolean pfEnabled;
}
