package com.juviai.user.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateTitleRequest {
    @NotBlank public String title;
    public LocalDate startDate;
    public LocalDate endDate;
}
