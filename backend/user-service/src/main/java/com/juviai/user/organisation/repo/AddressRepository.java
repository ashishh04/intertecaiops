package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    Page<Address> findByB2bUnit_IdAndTenantId(UUID b2bUnitId, String tenantId, Pageable pageable);

    Page<Address> findByUser_IdAndTenantId(UUID userId, String tenantId, Pageable pageable);
}
