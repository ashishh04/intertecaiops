package com.juviai.payroll.service.impl;

import com.juviai.payroll.domain.SalaryStructure;
import com.juviai.payroll.repo.SalaryStructureRepository;
import com.juviai.payroll.service.SalaryStructureService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository repository;

    @Override
    @Transactional
    public SalaryStructure create(SalaryStructure structure) {
        if (structure.getEmployeeId() == null)
            throw new IllegalArgumentException("employeeId is required");
        if (structure.getEffectiveFrom() == null)
            throw new IllegalArgumentException("effectiveFrom is required");

        // Deactivate any existing active structure before creating a new one
        repository.findFirstByEmployeeIdAndActiveTrueOrderByEffectiveFromDesc(structure.getEmployeeId())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    existing.setEffectiveTo(structure.getEffectiveFrom().minusDays(1));
                    repository.save(existing);
                    log.info("Deactivated salary structure {} for employee {}",
                            existing.getId(), structure.getEmployeeId());
                });

        structure.setActive(true);
        SalaryStructure saved = repository.save(structure);
        log.info("Created salary structure {} for employee {}", saved.getId(), saved.getEmployeeId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructure getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Salary structure not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructure getActive(UUID employeeId) {
        return repository.findFirstByEmployeeIdAndActiveTrueOrderByEffectiveFromDesc(employeeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active salary structure found for employee: " + employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryStructure> listHistory(UUID employeeId) {
        return repository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);
    }

    @Override
    @Transactional
    public SalaryStructure revise(UUID employeeId, SalaryStructure newStructure) {
        newStructure.setEmployeeId(employeeId);
        return create(newStructure); // create() handles deactivation of previous
    }

    @Override
    @Transactional
    public void deactivate(UUID structureId) {
        SalaryStructure structure = getById(structureId);
        structure.setActive(false);
        structure.setEffectiveTo(LocalDate.now());
        repository.save(structure);
        log.info("Deactivated salary structure {}", structureId);
    }
}
