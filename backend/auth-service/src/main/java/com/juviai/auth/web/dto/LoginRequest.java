package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank public String username;
    @NotBlank public String password;
    @NotBlank public String deviceId;
}
