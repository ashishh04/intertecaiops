package com.juviai.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class RegisterRequest {
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    @NotBlank @Email public String email;
    public String linkedinProfile;
    public String mobile;
    @NotBlank public String password;
    public Boolean active;
    public String status;
    public boolean student;
    public UUID collegeUUID;
    public String startYear;
    public String endYear;
    public String branchCode;
}
