package com.juviai.user.converter.populator.profile;

import com.juviai.user.converter.populator.ProfileExperiencePopulator;
import com.juviai.user.domain.ProfileExperience;
import com.juviai.user.dto.ProfileExperienceData;
import org.springframework.stereotype.Component;

@Component
public class ProfileExperienceBasicPopulator implements ProfileExperiencePopulator {
    @Override
    public void populate(ProfileExperience source, ProfileExperienceData target) {
        target.setId(source.getId());
        target.setType(source.getType());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setOrganizationName(source.getOrganizationName());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        if (source.getUser() != null) {
            target.setUserId(source.getUser().getId());
        }
        target.setVerificationStatus(source.getVerificationStatus());
        if (source.getVerifierB2bUnit() != null) {
            target.setVerifierB2bUnitId(source.getVerifierB2bUnit().getId());
        }
        if (source.getVerifiedBy() != null) {
            target.setVerifiedByUserId(source.getVerifiedBy().getId());
        }
        target.setVerifiedAt(source.getVerifiedAt());
    }
}
