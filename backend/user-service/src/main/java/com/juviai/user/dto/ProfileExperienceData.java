package com.juviai.user.dto;

import com.juviai.user.domain.ExperienceType;
import com.juviai.user.domain.VerificationStatus;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProfileExperienceData {
    private UUID id;
    private ExperienceType type;
    private String title;
    private String description;
    private String organizationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID userId;
    private VerificationStatus verificationStatus;
    private UUID verifierB2bUnitId;
    private UUID verifiedByUserId;
    private Instant verifiedAt;
}
