package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.domain.B2BUnitStatus;
import com.juviai.user.organisation.domain.B2BUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface B2BUnitRepository extends JpaRepository<B2BUnit, UUID> {
    List<B2BUnit> findByStatus(B2BUnitStatus status);
    Page<B2BUnit> findByStatus(B2BUnitStatus status, Pageable pageable);
    boolean existsByNameIgnoreCaseAndTenantId(String name, String tenantId);
    java.util.Optional<B2BUnit> findByNameIgnoreCaseAndTenantId(String name, String tenantId);
    boolean existsByCategoryId(UUID categoryId);

    Page<B2BUnit> findByTenantIdAndTypeAndNameContainingIgnoreCase(String tenantId, B2BUnitType type, String name, Pageable pageable);

    Page<B2BUnit> findByTenantId(String tenantId, Pageable pageable);

    @Query("select b from B2BUnit b " +
            "where b.tenantId = :tenantId and (" +
            " lower(b.name) like lower(concat('%', :q, '%'))" +
            " or lower(b.companyCode) like lower(concat('%', :q, '%'))" +
            " or lower(b.contactEmail) like lower(concat('%', :q, '%'))" +
            ")")
    Page<B2BUnit> searchAdmin(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
