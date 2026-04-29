package com.juviai.user.dto;

import com.juviai.user.domain.EmploymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
public class CreateEmployeeRequestDto {
    @NotNull private UUID b2bUnitId;
    private UUID storeId;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    private String mobile;
    private UUID designation;
    private UUID departmentId;
    private EmploymentType employmentType;
    private LocalDate hireDate;
    private UUID reportingManagerId;
    private UUID bandId;
    private BigDecimal annualSalary;
    @NotEmpty private List<UUID> roleIds;
}
