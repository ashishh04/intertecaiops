package com.juviai.user.domain;

import java.time.Instant;
import java.time.LocalDate;

import com.juviai.common.orm.BaseEntity;
import com.juviai.common.crypto.EncryptedStringConverter;
import com.juviai.user.organisation.domain.B2BUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user's work experience, project, or internship in their profile.
 */
@Entity
@Table(name = "profile_experiences")
@Getter
@Setter
@NoArgsConstructor
public class ProfileExperience extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExperienceType type; // PROJECT or INTERNSHIP

    // Stored encrypted — user profile data is PII under GDPR/DPDP
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String title;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String organizationName; // company/college/team name

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @ManyToOne
    @JoinColumn(name = "verifier_b2b_unit_id")
    private B2BUnit verifierB2bUnit; // business requested to verify

    @ManyToOne
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    private Instant verifiedAt;
}
