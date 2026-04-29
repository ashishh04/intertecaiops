package com.juviai.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateUserAddressRequest {

    private String name;

    private String addressType;

    private String mobileNumber;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotNull
    private UUID stateId;

    @NotNull
    private UUID cityId;

    private String country;

    @NotBlank
    private String postalCode;

    private String fullText;

    // Optional user fields to update (only if provided)
    private String firstName;
    private String lastName;
    private String mobile;
    private String linkedinProfile;
}
