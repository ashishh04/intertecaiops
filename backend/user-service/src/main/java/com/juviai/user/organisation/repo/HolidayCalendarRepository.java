package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.HolidayCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, UUID> {
    boolean existsByCity_IdAndTenantId(UUID cityId, String tenantId);

    Optional<HolidayCalendar> findByCity_IdAndTenantId(UUID cityId, String tenantId);

    Optional<HolidayCalendar> findByIdAndB2bUnit_IdAndTenantId(UUID id, UUID b2bUnitId, String tenantId);

    Page<HolidayCalendar> findByB2bUnit_IdAndTenantId(UUID b2bUnitId, String tenantId, Pageable pageable);
}
