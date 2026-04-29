package com.juviai.user.facade.impl;

import com.juviai.user.converter.EducationConverter;
import com.juviai.user.converter.ProfileExperienceConverter;
import com.juviai.user.converter.TitleRecordConverter;
import com.juviai.user.converter.UserSkillConverter;
import com.juviai.user.dto.EducationData;
import com.juviai.user.dto.ProfileExperienceData;
import com.juviai.user.dto.TitleRecordData;
import com.juviai.user.dto.UserSkillData;
import com.juviai.user.facade.ProfileFacade;
import com.juviai.user.service.ProfileService;
import com.juviai.user.web.dto.CreateEducationRequest;
import com.juviai.user.web.dto.CreateExperienceRequest;
import com.juviai.user.web.dto.CreateSkillRequest;
import com.juviai.user.web.dto.CreateTitleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileFacadeImpl implements ProfileFacade {

    private final ProfileService profileService;
    private final ProfileExperienceConverter profileExperienceConverter;
    private final UserSkillConverter userSkillConverter;
    private final EducationConverter educationConverter;
    private final TitleRecordConverter titleRecordConverter;

    @Override
    public ProfileExperienceData addExperience(UUID userId, CreateExperienceRequest request) {
        return profileExperienceConverter.convert(profileService.addExperience(userId, request));
    }

    @Override
    public List<ProfileExperienceData> myExperiences(UUID userId) {
        return profileExperienceConverter.convertAll(profileService.myExperiences(userId));
    }

    @Override
    public Optional<ProfileExperienceData> requestVerification(UUID id, UUID verifierB2bUnitId, UUID userId) {
        return profileService.requestVerification(id, verifierB2bUnitId, userId)
                .map(profileExperienceConverter::convert);
    }

    @Override
    public Optional<ProfileExperienceData> verifyExperience(UUID id, boolean approve, UUID userId) {
        return profileService.verifyExperience(id, approve, userId)
                .map(profileExperienceConverter::convert);
    }

    @Override
    public UserSkillData addSkill(UUID userId, CreateSkillRequest request) {
        return userSkillConverter.convert(profileService.addSkill(userId, request.name, request.level));
    }

    @Override
    public List<UserSkillData> mySkills(UUID userId) {
        return userSkillConverter.convertAll(profileService.mySkills(userId));
    }

    @Override
    public void deleteSkill(UUID userId, UUID skillId) {
        profileService.deleteSkill(userId, skillId);
    }

    @Override
    public EducationData addEducation(UUID userId, CreateEducationRequest request) {
        return educationConverter.convert(
                profileService.addEducation(userId, request.institution, request.degree,
                        request.fieldOfStudy, request.startDate, request.endDate));
    }

    @Override
    public List<EducationData> myEducation(UUID userId) {
        return educationConverter.convertAll(profileService.myEducation(userId));
    }

    @Override
    public TitleRecordData addTitle(UUID userId, CreateTitleRequest request) {
        return titleRecordConverter.convert(
                profileService.addTitle(userId, request.title, request.startDate, request.endDate));
    }

    @Override
    public List<TitleRecordData> myTitles(UUID userId) {
        return titleRecordConverter.convertAll(profileService.myTitles(userId));
    }
}
