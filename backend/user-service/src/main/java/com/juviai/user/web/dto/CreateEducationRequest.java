package com.juviai.user.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateEducationRequest {
    @NotBlank public String institution;
    @NotBlank public String degree;
    public String fieldOfStudy;
    public LocalDate startDate;
    public LocalDate endDate;
}
