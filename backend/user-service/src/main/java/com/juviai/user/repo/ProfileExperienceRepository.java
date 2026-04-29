package com.juviai.user.repo;

import com.juviai.user.domain.ProfileExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfileExperienceRepository extends JpaRepository<ProfileExperience, UUID> {
    List<ProfileExperience> findByUser_Id(UUID userId);
}
