package com.juviai.user.web;

import com.juviai.user.dto.EducationData;
import com.juviai.user.dto.ProfileExperienceData;
import com.juviai.user.dto.TitleRecordData;
import com.juviai.user.dto.UserSkillData;
import com.juviai.user.facade.ProfileFacade;
import com.juviai.user.web.dto.CreateEducationRequest;
import com.juviai.user.web.dto.CreateExperienceRequest;
import com.juviai.user.web.dto.CreateSkillRequest;
import com.juviai.user.web.dto.CreateTitleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@Validated
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileFacade profileFacade;

    /**
     * GatewayAuthFilter sets auth.getName() to the userId UUID (from X-User-Id header).
     * Parse it once here rather than treating it as an email.
     */
    private static UUID currentUserId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }

    // Experiences (Project/Internship)
    @PostMapping("/experiences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileExperienceData> addExperience(
            @RequestBody CreateExperienceRequest req, Authentication auth) {
        return ResponseEntity.ok(profileFacade.addExperience(currentUserId(auth), req));
    }

    @GetMapping("/experiences")
    @PreAuthorize("isAuthenticated()")
    public List<ProfileExperienceData> myExperiences(Authentication auth) {
        return profileFacade.myExperiences(currentUserId(auth));
    }

    @PostMapping("/experiences/{id}/request-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> requestVerification(
            @PathVariable("id") @NonNull UUID id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        UUID verifierB2b = body.get("verifierB2bUnitId") != null
                ? UUID.fromString(body.get("verifierB2bUnitId")) : null;
        return profileFacade.requestVerification(id, verifierB2b, currentUserId(auth))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/experiences/{id}/verify")
    @PreAuthorize("hasAnyRole('HR_HEAD','HR_RECRUITER','HR_MANAGER','ADMIN')")
    public ResponseEntity<?> verifyExperience(
            @PathVariable("id") @NonNull UUID id,
            @RequestParam("approve") boolean approve,
            Authentication auth) {
        return profileFacade.verifyExperience(id, approve, currentUserId(auth))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Skills
    @PostMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSkillData> addSkill(
            @RequestBody CreateSkillRequest req, Authentication auth) {
        return ResponseEntity.ok(profileFacade.addSkill(currentUserId(auth), req));
    }

    @GetMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public List<UserSkillData> mySkills(Authentication auth) {
        return profileFacade.mySkills(currentUserId(auth));
    }

    @DeleteMapping("/skills/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteSkill(
            @PathVariable("id") @NonNull UUID id, Authentication auth) {
        profileFacade.deleteSkill(currentUserId(auth), id);
        return ResponseEntity.<Void>noContent().build();
    }

    // Education
    @PostMapping("/education")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EducationData> addEducation(
            @RequestBody CreateEducationRequest req, Authentication auth) {
        return ResponseEntity.ok(profileFacade.addEducation(currentUserId(auth), req));
    }

    @GetMapping("/education")
    @PreAuthorize("isAuthenticated()")
    public List<EducationData> myEducation(Authentication auth) {
        return profileFacade.myEducation(currentUserId(auth));
    }

    // Titles
    @PostMapping("/titles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TitleRecordData> addTitle(
            @RequestBody CreateTitleRequest req, Authentication auth) {
        return ResponseEntity.ok(profileFacade.addTitle(currentUserId(auth), req));
    }

    @GetMapping("/titles")
    @PreAuthorize("isAuthenticated()")
    public List<TitleRecordData> myTitles(Authentication auth) {
        return profileFacade.myTitles(currentUserId(auth));
    }
}
