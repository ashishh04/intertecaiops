package com.juviai.leave.service;

import com.juviai.leave.domain.PublicHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface HolidayService {
    PublicHoliday create(PublicHoliday holiday);
    List<PublicHoliday> listByOrg(UUID b2bUnitId, int year);
    Set<LocalDate> getHolidayDates(UUID b2bUnitId, LocalDate from, LocalDate to);
    void delete(UUID id);
}
