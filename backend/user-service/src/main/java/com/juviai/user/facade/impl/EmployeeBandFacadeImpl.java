package com.juviai.user.facade.impl;

import com.juviai.user.converter.EmployeeBandConverter;
import com.juviai.user.domain.EmployeeOrgBand;
import com.juviai.user.facade.EmployeeBandFacade;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.service.EmployeeBandService;
import com.juviai.user.web.dto.EmployeeBandRequestDTO;
import com.juviai.user.web.dto.EmployeeBandResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeeBandFacadeImpl implements EmployeeBandFacade {

    private final EmployeeBandService employeeBandService;
    private final EmployeeBandConverter employeeBandConverter;

    @Override
    public EmployeeBandResponseDTO createBand(EmployeeBandRequestDTO request) {
        EmployeeOrgBand band = new EmployeeOrgBand();
        band.setName(request.getName());
        band.setExperienceMin(request.getExperienceMin());
        band.setExperienceMax(request.getExperienceMax());
        band.setStartingSalary(request.getStartingSalary());
        band.setEndingSalary(request.getEndingSalary());
        B2BUnit b2bUnit = new B2BUnit();
        b2bUnit.setId(request.getB2bUnitId());
        band.setB2bUnit(b2bUnit);
        return employeeBandConverter.convert(employeeBandService.createBand(band));
    }

    @Override
    public List<EmployeeBandResponseDTO> getBandsByB2bUnit(UUID b2bUnitId) {
        return employeeBandConverter.convertAll(employeeBandService.getBandsByB2bUnit(b2bUnitId));
    }

    @Override
    public EmployeeBandResponseDTO getBand(UUID id) {
        return employeeBandConverter.convert(employeeBandService.getBand(id));
    }

    @Override
    public EmployeeBandResponseDTO updateBand(UUID id, EmployeeBandRequestDTO request) {
        EmployeeOrgBand updates = new EmployeeOrgBand();
        updates.setName(request.getName());
        updates.setExperienceMin(request.getExperienceMin());
        updates.setExperienceMax(request.getExperienceMax());
        updates.setStartingSalary(request.getStartingSalary());
        updates.setEndingSalary(request.getEndingSalary());
        return employeeBandConverter.convert(employeeBandService.updateBand(id, updates));
    }

    @Override
    public void deleteBand(UUID id) {
        employeeBandService.deleteBand(id);
    }
}
