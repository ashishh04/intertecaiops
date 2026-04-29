package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {
    @NotBlank public String refreshToken;
    @NotBlank public String deviceId;
}
