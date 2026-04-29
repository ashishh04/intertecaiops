package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class VerifyOtpRequest {
    @NotNull public UUID userId;
    @NotBlank public String otp;
}
