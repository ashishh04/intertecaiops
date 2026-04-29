package com.juviai.leave.service.impl;

import com.juviai.leave.domain.CompOff;
import com.juviai.leave.domain.CompOffStatus;
import com.juviai.leave.repo.CompOffRepository;
import com.juviai.leave.service.CompOffService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompOffServiceImpl implements CompOffService {

    private static final int COMP_OFF_EXPIRY_DAYS = 90;

    private final CompOffRepository repository;

    @Override
    @Transactional
    public CompOff request(CompOff compOff) {
        if (compOff.getEmployeeId() == null) throw new IllegalArgumentException("employeeId is required");
        if (compOff.getWorkedDate() == null) throw new IllegalArgumentException("workedDate is required");
        if (compOff.getWorkedDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("workedDate cannot be in the future");

        compOff.setStatus(CompOffStatus.PENDING);
        if (compOff.getExpiresAt() == null) {
            compOff.setExpiresAt(LocalDate.now().plusDays(COMP_OFF_EXPIRY_DAYS));
        }
        CompOff saved = repository.save(compOff);
        log.info("Comp-off requested by employee {} for date {}", saved.getEmployeeId(), saved.getWorkedDate());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public CompOff getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comp-off not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompOff> listByEmployee(UUID employeeId) {
        return repository.findByEmployeeIdOrderByWorkedDateDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompOff> listPending() {
        return repository.findByStatusOrderByCreatedAtAsc(CompOffStatus.PENDING);
    }

    @Override
    @Transactional
    public CompOff approve(UUID id, UUID approvedBy) {
        CompOff compOff = getById(id);
        if (compOff.getStatus() != CompOffStatus.PENDING) {
            throw new IllegalStateException("Only PENDING comp-offs can be approved. Status: " + compOff.getStatus());
        }
        compOff.setStatus(CompOffStatus.APPROVED);
        compOff.setApprovedBy(approvedBy);
        compOff.setApprovedAt(Instant.now());
        log.info("Comp-off {} approved by {}", id, approvedBy);
        return repository.save(compOff);
    }

    @Override
    @Transactional
    public CompOff reject(UUID id, UUID rejectedBy) {
        CompOff compOff = getById(id);
        if (compOff.getStatus() != CompOffStatus.PENDING) {
            throw new IllegalStateException("Only PENDING comp-offs can be rejected. Status: " + compOff.getStatus());
        }
        compOff.setStatus(CompOffStatus.REJECTED);
        compOff.setApprovedBy(rejectedBy);
        compOff.setApprovedAt(Instant.now());
        log.info("Comp-off {} rejected by {}", id, rejectedBy);
        return repository.save(compOff);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal availableCredits(UUID employeeId) {
        return repository.sumAvailableCredits(employeeId, LocalDate.now());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")  // runs daily at 01:00
    public void expireStale() {
        List<CompOff> expired = repository.findExpired(LocalDate.now());
        expired.forEach(c -> c.setStatus(CompOffStatus.EXPIRED));
        repository.saveAll(expired);
        if (!expired.isEmpty()) {
            log.info("Expired {} stale comp-off records", expired.size());
        }
    }
}
