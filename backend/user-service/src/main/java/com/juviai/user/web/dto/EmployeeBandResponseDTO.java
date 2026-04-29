package com.juviai.user.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeBandResponseDTO {
    private UUID id;
    private String name;
    private Integer experienceMin;
    private Integer experienceMax;
    private UUID b2bUnitId;
    private Double startingSalary;
    private Double endingSalary;
}
