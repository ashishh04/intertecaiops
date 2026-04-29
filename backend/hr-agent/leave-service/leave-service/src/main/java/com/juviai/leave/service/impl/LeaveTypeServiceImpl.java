package com.juviai.leave.service.impl;

import com.juviai.leave.domain.LeaveType;
import com.juviai.leave.repo.LeaveTypeRepository;
import com.juviai.leave.service.LeaveTypeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository repository;

    @Override
    @Transactional
    public LeaveType create(LeaveType leaveType) {
        if (leaveType.getB2bUnitId() == null)
            throw new IllegalArgumentException("b2bUnitId is required");
        if (leaveType.getCode() == null || leaveType.getCode().isBlank())
            throw new IllegalArgumentException("code is required");
        if (repository.existsByB2bUnitIdAndCodeIgnoreCase(leaveType.getB2bUnitId(), leaveType.getCode()))
            throw new IllegalStateException("Leave type with code '" + leaveType.getCode()
                    + "' already exists for this organisation");

        leaveType.setActive(true);
        LeaveType saved = repository.save(leaveType);
        log.info("Created leave type {} ({}) for org {}", saved.getCode(), saved.getId(), saved.getB2bUnitId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveType getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveType getByCode(UUID b2bUnitId, String code) {
        return repository.findByB2bUnitIdAndCodeIgnoreCase(b2bUnitId, code)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Leave type '" + code + "' not found for org " + b2bUnitId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveType> listActive(UUID b2bUnitId) {
        return repository.findByB2bUnitIdAndActiveTrueOrderByName(b2bUnitId);
    }

    @Override
    @Transactional
    public LeaveType update(UUID id, LeaveType updated) {
        LeaveType existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPaid(updated.isPaid());
        existing.setRequiresDocument(updated.isRequiresDocument());
        existing.setMaxConsecutiveDays(updated.getMaxConsecutiveDays());
        existing.setCarryForwardAllowed(updated.isCarryForwardAllowed());
        existing.setMaxCarryForwardDays(updated.getMaxCarryForwardDays());
        existing.setEncashable(updated.isEncashable());
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        LeaveType lt = getById(id);
        lt.setActive(false);
        repository.save(lt);
        log.info("Deactivated leave type {}", id);
    }
}
