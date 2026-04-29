package com.juviai.leave.repo;

import com.juviai.leave.domain.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, UUID> {

    List<PublicHoliday> findByB2bUnitIdAndHolidayDateBetweenOrderByHolidayDate(
            UUID b2bUnitId, LocalDate from, LocalDate to);

    List<PublicHoliday> findByB2bUnitIdAndHolidayDateBetweenAndRegionIsNullOrRegionOrderByHolidayDate(
            UUID b2bUnitId, LocalDate from, LocalDate to, String region);

    boolean existsByB2bUnitIdAndHolidayDate(UUID b2bUnitId, LocalDate date);
}
