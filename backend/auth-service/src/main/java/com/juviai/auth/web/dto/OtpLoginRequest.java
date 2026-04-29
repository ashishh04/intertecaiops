package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /auth/otp/login.
 * Submits the OTP received via SMS together with the phone number.
 * On success the server returns a {@link TokenResponse}.
 * If the phone number has no existing account, one is created automatically.
 */
public class OtpLoginRequest {

    /** E.164 phone number used to identify the user. */
    @NotBlank(message = "primaryContact is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number format")
    public String primaryContact;

    /** 6-digit OTP received via SMS. */
    @NotBlank(message = "otp is required")
    public String otp;

    /**
     * Display name — only used when auto-registering a new user.
     * Ignored if the user already exists.
     */
    public String fullName;

    /** Device fingerprint for refresh-token session binding. */
    public String deviceId;
}
