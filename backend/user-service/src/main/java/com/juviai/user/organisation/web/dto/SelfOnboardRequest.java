package com.juviai.user.organisation.web.dto;

import com.juviai.user.organisation.domain.B2BUnitType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfOnboardRequest {
    @NotBlank private String name;
    @NotNull private B2BUnitType type;
    @Email private String contactEmail;
    private String contactPhone;
    private String website;
    private AddressDTO address;
    private String groupName; // optional
}
