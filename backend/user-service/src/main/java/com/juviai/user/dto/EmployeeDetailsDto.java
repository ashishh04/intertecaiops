package com.juviai.user.dto;

import com.juviai.user.domain.Gender;
import com.juviai.user.domain.EmploymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailsDto {
    private UUID id;
    private UUID b2bUnitId;
    private UUID storeId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String employeeCode;

    private Boolean dismissed;

    private LocalDate dateOfBirth;
    private Gender gender;
    private BigDecimal annualSalary;

    private UUID designationId;
    private String designation;

    private UUID departmentId;
    private String department;

    private UUID bandId;
    private String band;

    private LocalDate hireDate;
    private EmploymentType employmentType;

    private UUID reportingManagerId;
    private UserBriefDto reportingManager;

    private List<UUID> roleIds;
    private List<String> roles;

    private String accountNumber;
    private String ifscCode;

    private String pfNumber;
    private String uanNumber;
    private String panNumber;
    private Boolean pfEnabled;

    private CompensationDto activeCompensation;
    private List<CompensationDto> compensationHistory;
}
