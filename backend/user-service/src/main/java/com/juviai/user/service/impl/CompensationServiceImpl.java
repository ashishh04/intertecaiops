package com.juviai.user.service.impl;

import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.CompensationType;
import com.juviai.user.repo.CompensationRepository;
import com.juviai.user.service.CompensationService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompensationServiceImpl implements CompensationService {

    private final CompensationRepository compensationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Compensation> listHistory(UUID employeeId) {
        return compensationRepository.findByEmployeeIdOrderByEffectiveStartDateDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Compensation getById(UUID id) {
        return compensationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compensation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compensation> getAll() {
        return compensationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Compensation getActive(UUID employeeId) {
        return compensationRepository.findFirstByEmployeeIdAndActiveTrueOrderByEffectiveStartDateDesc(employeeId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAny(UUID employeeId) {
        return compensationRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAny(UUID employeeId, CompensationType type) {
        return compensationRepository.existsByEmployeeIdAndType(employeeId, type);
    }

    @Override
    @Transactional(readOnly = true)
    public Compensation getActive(UUID employeeId, CompensationType type) {
        return compensationRepository.findFirstByEmployeeIdAndTypeAndActiveTrueOrderByEffectiveStartDateDesc(employeeId, type)
                .orElse(null);
    }

    @Override
    @Transactional
    public Compensation create(Compensation compensation) {
        if (compensation == null) {
            throw new IllegalArgumentException("compensation is required");
        }
        return compensationRepository.save(compensation);
    }

    @Override
    @Transactional
    public Compensation reviseActiveCompensation(UUID employeeId,
                                                CompensationType type,
                                                Compensation nextCompensation) {
        if (nextCompensation == null) {
            throw new IllegalArgumentException("nextCompensation is required");
        }

        LocalDate nextStart = nextCompensation.getEffectiveStartDate();
        if (nextStart == null) {
            throw new IllegalArgumentException("nextCompensation.effectiveStartDate is required");
        }

        Compensation active = compensationRepository.findFirstByEmployeeIdAndTypeAndActiveTrueOrderByEffectiveStartDateDesc(employeeId, type)
                .orElseThrow(() -> new IllegalArgumentException("No active compensation found"));

        active.setActive(false);
        active.setEffectiveEndDate(nextStart.minusDays(1));
        compensationRepository.save(active);

        if (nextCompensation.getEmployee() == null) {
            nextCompensation.setEmployee(active.getEmployee());
        }
        if (nextCompensation.getType() == null) {
            nextCompensation.setType(type);
        }
        nextCompensation.setEffectiveEndDate(null);
        nextCompensation.setActive(true);
        return create(nextCompensation);
    }
}
