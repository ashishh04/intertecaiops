package com.juviai.user.organisation.repo;

import com.juviai.user.organisation.domain.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    boolean existsByHolidayCalendar_IdAndDateAndTenantId(UUID holidayCalendarId, LocalDate date, String tenantId);

    Page<Holiday> findByHolidayCalendar_IdAndTenantIdOrderByDateAsc(UUID holidayCalendarId, String tenantId, Pageable pageable);

    long deleteByIdAndHolidayCalendar_IdAndTenantId(UUID id, UUID holidayCalendarId, String tenantId);
}
