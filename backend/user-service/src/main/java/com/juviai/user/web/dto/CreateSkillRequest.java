package com.juviai.user.web.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateSkillRequest {
    @NotBlank public String name;
    public String level;
}
