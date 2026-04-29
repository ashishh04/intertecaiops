package com.juviai.user.facade;

import com.juviai.user.dto.EducationData;
import com.juviai.user.dto.ProfileExperienceData;
import com.juviai.user.dto.TitleRecordData;
import com.juviai.user.dto.UserSkillData;
import com.juviai.user.web.dto.CreateEducationRequest;
import com.juviai.user.web.dto.CreateExperienceRequest;
import com.juviai.user.web.dto.CreateSkillRequest;
import com.juviai.user.web.dto.CreateTitleRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileFacade {
    ProfileExperienceData addExperience(UUID userId, CreateExperienceRequest request);
    List<ProfileExperienceData> myExperiences(UUID userId);
    Optional<ProfileExperienceData> requestVerification(UUID id, UUID verifierB2bUnitId, UUID userId);
    Optional<ProfileExperienceData> verifyExperience(UUID id, boolean approve, UUID userId);

    UserSkillData addSkill(UUID userId, CreateSkillRequest request);
    List<UserSkillData> mySkills(UUID userId);
    void deleteSkill(UUID userId, UUID skillId);

    EducationData addEducation(UUID userId, CreateEducationRequest request);
    List<EducationData> myEducation(UUID userId);

    TitleRecordData addTitle(UUID userId, CreateTitleRequest request);
    List<TitleRecordData> myTitles(UUID userId);
}
