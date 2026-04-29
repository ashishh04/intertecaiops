package com.juviai.user.repo;

import com.juviai.user.domain.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {
    List<Experience> findByUser_Id(UUID userId);
}
