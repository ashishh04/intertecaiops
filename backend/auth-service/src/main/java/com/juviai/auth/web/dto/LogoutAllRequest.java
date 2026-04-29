package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutAllRequest {
    @NotBlank public String userId;
}
