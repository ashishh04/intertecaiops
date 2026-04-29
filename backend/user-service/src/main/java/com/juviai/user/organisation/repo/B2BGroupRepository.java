package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.B2BGroup;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface B2BGroupRepository extends JpaRepository<B2BGroup, UUID> {
    Optional<B2BGroup> findByCodeAndTenantId(String name, String tenantId);
}
