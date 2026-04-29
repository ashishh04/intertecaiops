package com.juviai.payroll.converter;

import com.juviai.payroll.domain.SalaryStructure;
import com.juviai.payroll.domain.SalaryStructureComponent;
import com.juviai.payroll.dto.PayrollDtos.SalaryStructureComponentDto;
import com.juviai.payroll.dto.PayrollDtos.SalaryStructureDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SalaryStructureConverter extends AbstractPopulatingConverter<SalaryStructure, SalaryStructureDto> {

    @Override
    protected SalaryStructureDto createTarget() {
        return new SalaryStructureDto();
    }

    @Override
    protected void populate(SalaryStructure source, SalaryStructureDto target) {
        target.setId(source.getId());
        target.setEmployeeId(source.getEmployeeId());
        target.setName(source.getName());
        target.setEffectiveFrom(source.getEffectiveFrom());
        target.setEffectiveTo(source.getEffectiveTo());
        target.setActive(source.isActive());
        target.setComponents(source.getComponents().stream()
                .map(this::toComponentDto)
                .collect(Collectors.toList()));
    }

    private SalaryStructureComponentDto toComponentDto(SalaryStructureComponent c) {
        SalaryStructureComponentDto dto = new SalaryStructureComponentDto();
        dto.setId(c.getId());
        dto.setComponentType(c.getComponentType());
        dto.setName(c.getName());
        dto.setCalculationType(c.getCalculationType());
        dto.setValue(c.getValue());
        dto.setTaxable(c.isTaxable());
        dto.setEarning(c.isEarning());
        dto.setSortOrder(c.getSortOrder());
        return dto;
    }
}
