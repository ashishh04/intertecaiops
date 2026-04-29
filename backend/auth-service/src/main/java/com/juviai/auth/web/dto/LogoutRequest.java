package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    @NotBlank public String refreshToken;
    @NotBlank public String deviceId;
}
