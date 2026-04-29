package com.juviai.payroll.converter;

import com.juviai.payroll.domain.Reimbursement;
import com.juviai.payroll.dto.PayrollDtos.ReimbursementDto;
import org.springframework.stereotype.Component;

@Component
public class ReimbursementConverter extends AbstractPopulatingConverter<Reimbursement, ReimbursementDto> {

    @Override
    protected ReimbursementDto createTarget() {
        return new ReimbursementDto();
    }

    @Override
    protected void populate(Reimbursement source, ReimbursementDto target) {
        target.setId(source.getId());
        target.setEmployeeId(source.getEmployeeId());
        target.setCategory(source.getCategory());
        target.setDescription(source.getDescription());
        target.setClaimAmount(source.getClaimAmount());
        target.setApprovedAmount(source.getApprovedAmount());
        target.setClaimDate(source.getClaimDate());
        target.setStatus(source.getStatus());
        target.setReceiptUrl(source.getReceiptUrl());
        target.setRemarks(source.getRemarks());
    }
}
