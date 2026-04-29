package com.juviai.payroll.service;

import com.juviai.payroll.domain.SalaryStructure;

import java.util.List;
import java.util.UUID;

public interface SalaryStructureService {

    SalaryStructure create(SalaryStructure structure);

    SalaryStructure getById(UUID id);

    SalaryStructure getActive(UUID employeeId);

    List<SalaryStructure> listHistory(UUID employeeId);

    /**
     * Deactivates the current active structure and creates a new one.
     * Used on salary revisions or designation changes.
     */
    SalaryStructure revise(UUID employeeId, SalaryStructure newStructure);

    void deactivate(UUID structureId);
}
