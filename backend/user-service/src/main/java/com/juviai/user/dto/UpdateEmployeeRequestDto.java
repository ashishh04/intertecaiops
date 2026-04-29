package com.juviai.user.dto;

import com.juviai.user.domain.EmploymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.juviai.user.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequestDto {
    private String firstName;
    private String lastName;
    private String mobile;
    private UUID storeId;
    private UUID designation;
    private UUID departmentId;
    private EmploymentType employmentType;
    private LocalDate hireDate;
    private UUID reportingManagerId;
    private UUID bandId;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String email;
    private BigDecimal annualSalary;
    private String pfNumber;
    private String uanNumber;
    private boolean pfEnabled;
    private String panNumber;
}
