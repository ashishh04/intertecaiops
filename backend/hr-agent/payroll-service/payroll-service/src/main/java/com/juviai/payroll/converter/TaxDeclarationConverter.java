package com.juviai.payroll.converter;

import com.juviai.payroll.domain.TaxDeclaration;
import com.juviai.payroll.dto.PayrollDtos.TaxDeclarationDto;
import org.springframework.stereotype.Component;

@Component
public class TaxDeclarationConverter extends AbstractPopulatingConverter<TaxDeclaration, TaxDeclarationDto> {

    @Override
    protected TaxDeclarationDto createTarget() {
        return new TaxDeclarationDto();
    }

    @Override
    protected void populate(TaxDeclaration source, TaxDeclarationDto target) {
        target.setId(source.getId());
        target.setEmployeeId(source.getEmployeeId());
        target.setFinancialYear(source.getFinancialYear());
        target.setSection(source.getSection());
        target.setDescription(source.getDescription());
        target.setDeclaredAmount(source.getDeclaredAmount());
        target.setApprovedAmount(source.getApprovedAmount());
        target.setStatus(source.getStatus());
        target.setDocumentUrl(source.getDocumentUrl());
    }
}
