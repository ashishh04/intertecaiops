package com.juviai.user.web.dto;

import com.juviai.user.domain.ExperienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateExperienceRequest {
    @NotNull public ExperienceType type; // PROJECT/INTERNSHIP
    @NotBlank public String title;
    public String description;
    public String organizationName;
    public LocalDate startDate;
    public LocalDate endDate;
    public Integer startMonth;
    public Integer endMonth;
    public String role;
}
