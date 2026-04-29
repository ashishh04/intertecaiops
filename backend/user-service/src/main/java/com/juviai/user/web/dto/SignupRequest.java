package com.juviai.user.web.dto;

import com.juviai.user.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Signup payload.
 *
 * <p>CLAUDE.md rule 1 — all users register with a mobile number first; persona
 * is derived from data added later. Email is optional; mobile is the required
 * primary identifier. When email is not supplied, the user's {@code username}
 * falls back to their mobile number.
 */
public class SignupRequest {
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    /** Optional — supplied later, or never (mobile-first persona). */
    @Email public String email;
    public String linkedinProfile;
    /** Required — mobile-first registration. */
    @NotBlank public String mobile;
    @NotBlank public String password;
    public Boolean active;
    public UserStatus status;
    public boolean student;
    public UUID collegeUUID;
    public String startYear;
    public String endYear;
    public String branchCode;
}
