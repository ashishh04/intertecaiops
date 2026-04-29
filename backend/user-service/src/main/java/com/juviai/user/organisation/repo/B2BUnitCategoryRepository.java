package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.B2BUnitCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.UUID;

public interface B2BUnitCategoryRepository extends JpaRepository<B2BUnitCategory, UUID> {

    Page<B2BUnitCategory> findByTenantId(String tenantId, Pageable pageable);

    Optional<B2BUnitCategory> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByCodeIgnoreCaseAndTenantId(String code, String tenantId);

    boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);
}
