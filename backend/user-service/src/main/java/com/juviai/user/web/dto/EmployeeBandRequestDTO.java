package com.juviai.user.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class EmployeeBandRequestDTO {
    private String name;
    private Integer experienceMin;
    private Integer experienceMax;
    private UUID b2bUnitId;
    private Double startingSalary;
    private Double endingSalary;
}
