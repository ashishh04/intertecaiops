package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ResendOtpRequest {
    @NotNull public UUID userId;
}
