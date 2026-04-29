package com.juviai.payroll.converter;

import com.juviai.payroll.domain.Payslip;
import com.juviai.payroll.domain.PayslipComponent;
import com.juviai.payroll.dto.PayrollDtos.PayslipComponentDto;
import com.juviai.payroll.dto.PayrollDtos.PayslipDto;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class PayslipConverter extends AbstractPopulatingConverter<Payslip, PayslipDto> {

    @Override
    protected PayslipDto createTarget() {
        return new PayslipDto();
    }

    @Override
    protected void populate(Payslip source, PayslipDto target) {
        target.setId(source.getId());
        target.setPayrollPeriodId(source.getPayrollPeriod().getId());
        target.setPeriodLabel(buildLabel(
                source.getPayrollPeriod().getPeriodYear(),
                source.getPayrollPeriod().getPeriodMonth()));
        target.setEmployeeId(source.getEmployeeId());
        target.setEmployeeCode(source.getEmployeeCode());
        target.setWorkingDays(source.getWorkingDays());
        target.setPaidDays(source.getPaidDays());
        target.setLopDays(source.getLopDays());
        target.setGrossEarnings(source.getGrossEarnings());
        target.setTotalDeductions(source.getTotalDeductions());
        target.setNetPay(source.getNetPay());
        target.setStatus(source.getStatus());
        target.setPaymentReference(source.getPaymentReference());

        List<PayslipComponentDto> earnings = source.getComponents().stream()
                .filter(PayslipComponent::isEarning)
                .map(this::toComponentDto)
                .collect(Collectors.toList());

        List<PayslipComponentDto> deductions = source.getComponents().stream()
                .filter(c -> !c.isEarning())
                .map(this::toComponentDto)
                .collect(Collectors.toList());

        target.setEarnings(earnings);
        target.setDeductions(deductions);
    }

    private PayslipComponentDto toComponentDto(PayslipComponent c) {
        PayslipComponentDto dto = new PayslipComponentDto();
        dto.setComponentType(c.getComponentType());
        dto.setName(c.getName());
        dto.setAmount(c.getAmount());
        dto.setEarning(c.isEarning());
        dto.setSortOrder(c.getSortOrder());
        return dto;
    }

    private String buildLabel(int year, int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
    }
}
