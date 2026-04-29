package com.juviai.user.service;

import com.juviai.user.domain.*;
import com.juviai.user.repo.*;
import com.juviai.user.web.dto.CreateExperienceRequest;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileExperienceRepository experienceRepository;
    private final UserSkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final TitleRecordRepository titleRepository;
    private final WalletClient walletClient;

    public ProfileService(UserRepository userRepository,
                          ProfileExperienceRepository experienceRepository,
                          UserSkillRepository skillRepository,
                          EducationRepository educationRepository,
                          TitleRecordRepository titleRepository,
                          WalletClient walletClient) {
        this.userRepository = userRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.educationRepository = educationRepository;
        this.titleRepository = titleRepository;
        this.walletClient = walletClient;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Resolves a User entity from its UUID. All profile operations receive a
     * userId — the gateway always propagates X-User-Id, never an email.
     */
    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UUID b2bUnitIdForUser(UUID userId) {
        return userRepository.findById(userId)
                .map(u -> u.getB2bUnit() != null ? u.getB2bUnit().getId() : null)
                .orElse(null);
    }

    // ── Experiences ───────────────────────────────────────────────────────────

    @Transactional
    public ProfileExperience addExperience(UUID userId, CreateExperienceRequest req) {
        User user = requireUser(userId);
        String tenantId = user.getTenantId() != null ? user.getTenantId() : "default";

        ProfileExperience experience = new ProfileExperience();
        experience.setUser(user);
        experience.setTenantId(tenantId);
        experience.setType(req.type);
        experience.setTitle(req.title);
        experience.setDescription(req.description);
        experience.setOrganizationName(req.organizationName);
        experience.setStartDate(req.startDate);
        experience.setEndDate(req.endDate);
        experience.setVerificationStatus(VerificationStatus.UNVERIFIED);
        experience = experienceRepository.save(experience);

        String cat = (req.type == ExperienceType.PROJECT) ? "PROJECT" : "INTERNSHIP";
        walletClient.award(user.getId(), tenantId, cat, req.type + " added", experience.getId());
        return experience;
    }

    @Transactional(readOnly = true)
    public List<ProfileExperience> myExperiences(UUID userId) {
        return experienceRepository.findByUser_Id(userId);
    }

    @Transactional
    public Optional<ProfileExperience> requestVerification(@NonNull UUID expId, UUID verifierB2bUnitId, UUID userId) {
        return experienceRepository.findById(expId).map(e -> {
            if (e.getUser() == null || !e.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Cannot request verification for other user's experience");
            }
            com.juviai.user.organisation.domain.B2BUnit bu = new com.juviai.user.organisation.domain.B2BUnit();
            bu.setId(verifierB2bUnitId);
            e.setVerifierB2bUnit(bu);
            e.setVerificationStatus(VerificationStatus.PENDING);
            return experienceRepository.save(e);
        });
    }

    @Transactional
    public Optional<ProfileExperience> verifyExperience(@NonNull UUID expId, boolean approve, UUID verifierUserId) {
        UUID callerB2B = b2bUnitIdForUser(verifierUserId);
        return experienceRepository.findById(expId).map(e -> {
            UUID verifierUnitId = e.getVerifierB2bUnit() != null ? e.getVerifierB2bUnit().getId() : null;
            if (verifierUnitId == null || !verifierUnitId.equals(callerB2B)) {
                throw new IllegalArgumentException("Not authorized to verify this experience");
            }
            e.setVerificationStatus(approve ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
            e.setVerifiedAt(Instant.now());
            User verifier = new User();
            verifier.setId(verifierUserId);
            e.setVerifiedBy(verifier);
            return experienceRepository.save(e);
        });
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    @Transactional
    public UserSkill addSkill(UUID userId, String name, String level) {
        UserSkill s = new UserSkill();
        s.setUserId(userId);
        s.setName(name);
        s.setLevel(level);
        s = skillRepository.save(s);

        User user = requireUser(userId);
        String tenantId = user.getTenantId() != null ? user.getTenantId() : "default";
        walletClient.award(userId, tenantId, "SKILL", "Skill added", s.getId());
        return s;
    }

    @Transactional(readOnly = true)
    public List<UserSkill> mySkills(UUID userId) {
        return skillRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteSkill(UUID userId, @NonNull UUID skillId) {
        skillRepository.findById(skillId).ifPresent(s -> {
            if (!s.getUserId().equals(userId)) throw new IllegalArgumentException("Cannot delete others' skill");
            skillRepository.delete(s);
        });
    }

    // ── Education ─────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    @Transactional
    public Education addEducation(UUID userId, String institution, String degree,
                                  String fieldOfStudy, java.time.LocalDate start, java.time.LocalDate end) {
        Education ed = new Education();
        ed.setUserId(userId);
        ed.setInstitution(institution);
        ed.setDegree(degree);
        ed.setFieldOfStudy(fieldOfStudy);
        ed.setStartDate(start);
        ed.setEndDate(end);
        ed = educationRepository.save(ed);

        User user = requireUser(userId);
        String tenantId = user.getTenantId() != null ? user.getTenantId() : "default";
        walletClient.award(userId, tenantId, "EDUCATION", "Education added", ed.getId());
        return ed;
    }

    @Transactional(readOnly = true)
    public List<Education> myEducation(UUID userId) {
        return educationRepository.findByUserId(userId);
    }

    // ── Titles ────────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    @Transactional
    public TitleRecord addTitle(UUID userId, String title, java.time.LocalDate start, java.time.LocalDate end) {
        TitleRecord t = new TitleRecord();
        t.setUserId(userId);
        t.setTitle(title);
        t.setStartDate(start);
        t.setEndDate(end);
        t = titleRepository.save(t);

        User user = requireUser(userId);
        String tenantId = user.getTenantId() != null ? user.getTenantId() : "default";
        walletClient.award(userId, tenantId, "TITLE", "Title added", t.getId());
        return t;
    }

    @Transactional(readOnly = true)
    public List<TitleRecord> myTitles(UUID userId) {
        return titleRepository.findByUserId(userId);
    }
}
