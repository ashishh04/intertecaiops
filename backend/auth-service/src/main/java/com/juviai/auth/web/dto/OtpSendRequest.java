package com.juviai.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /auth/otp/send.
 * Triggers OTP generation and dispatch (SMS) for the given phone number.
 */
public class OtpSendRequest {

    /** E.164 phone number, e.g. "+919876543210". */
    @NotBlank(message = "primaryContact is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number format")
    public String primaryContact;
}
