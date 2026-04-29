package com.juviai.user.organisation.web.dto;

import com.juviai.user.organisation.domain.B2BUnitType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnboardRequest {
    @NotBlank private String name;
    @NotNull private B2BUnitType type;
    @Email private String contactEmail;
    private String contactPhone;
    private String website;
    private String logo;
    private Map<String, String> additionalAttributes;
    private AddressDTO address;
    private String groupName; // optional
    private String startupDescription;
    // Admin-specific (optional for self onboard)
    private String approver;
    private String adminFirstName;
    private String adminLastName;
    @Email private String adminEmail;
    private String adminMobile;
    private String brandTagLine;
    private UUID category;
    private String targetAudience;
    private String revenueModel;
    private boolean findCoFounder;
    private boolean buildSolo;
    private boolean inviteCoFounder;
    private boolean studentStartup;
    private boolean adminOnboardRequest;
}
