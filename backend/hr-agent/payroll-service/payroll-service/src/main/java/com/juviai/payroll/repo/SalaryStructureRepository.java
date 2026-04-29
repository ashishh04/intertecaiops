package com.juviai.payroll.repo;

import com.juviai.payroll.domain.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {

    List<SalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    Optional<SalaryStructure> findFirstByEmployeeIdAndActiveTrueOrderByEffectiveFromDesc(UUID employeeId);

    boolean existsByEmployeeIdAndActiveTrue(UUID employeeId);
}
