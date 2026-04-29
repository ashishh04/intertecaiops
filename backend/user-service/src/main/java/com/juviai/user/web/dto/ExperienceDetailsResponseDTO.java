package com.juviai.user.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExperienceDetailsResponseDTO {
    private String experienceType;
    private ExperienceB2BUnitDTO b2bUnit;
    private UUID departmentId;
    private String departmentCode;
    private Integer startYear;
    private Integer endYear;
}
