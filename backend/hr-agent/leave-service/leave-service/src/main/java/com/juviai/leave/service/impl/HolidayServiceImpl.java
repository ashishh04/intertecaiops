package com.juviai.leave.service.impl;

import com.juviai.leave.domain.PublicHoliday;
import com.juviai.leave.repo.PublicHolidayRepository;
import com.juviai.leave.service.HolidayService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayServiceImpl implements HolidayService {

    private final PublicHolidayRepository repository;

    @Override
    @Transactional
    public PublicHoliday create(PublicHoliday holiday) {
        if (holiday.getB2bUnitId() == null) throw new IllegalArgumentException("b2bUnitId is required");
        if (holiday.getHolidayDate() == null) throw new IllegalArgumentException("holidayDate is required");
        if (holiday.getName() == null || holiday.getName().isBlank())
            throw new IllegalArgumentException("name is required");

        PublicHoliday saved = repository.save(holiday);
        log.info("Created holiday {} on {} for org {}", saved.getName(), saved.getHolidayDate(), saved.getB2bUnitId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicHoliday> listByOrg(UUID b2bUnitId, int year) {
        return repository.findByB2bUnitIdAndHolidayDateBetweenOrderByHolidayDate(
                b2bUnitId,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> getHolidayDates(UUID b2bUnitId, LocalDate from, LocalDate to) {
        return repository
                .findByB2bUnitIdAndHolidayDateBetweenOrderByHolidayDate(b2bUnitId, from, to)
                .stream()
                .map(PublicHoliday::getHolidayDate)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PublicHoliday h = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Holiday not found: " + id));
        repository.delete(h);
        log.info("Deleted holiday {}", id);
    }
}
