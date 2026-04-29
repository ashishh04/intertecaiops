package com.juviai.payroll.converter;

import com.juviai.payroll.domain.PayrollPeriod;
import com.juviai.payroll.dto.PayrollDtos.PayrollPeriodDto;
import com.juviai.payroll.repo.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PayrollPeriodConverter extends AbstractPopulatingConverter<PayrollPeriod, PayrollPeriodDto> {

    private final PayslipRepository payslipRepository;

    @Override
    protected PayrollPeriodDto createTarget() {
        return new PayrollPeriodDto();
    }

    @Override
    protected void populate(PayrollPeriod source, PayrollPeriodDto target) {
        target.setId(source.getId());
        target.setB2bUnitId(source.getB2bUnitId());
        target.setPeriodYear(source.getPeriodYear());
        target.setPeriodMonth(source.getPeriodMonth());
        target.setPeriodLabel(buildLabel(source.getPeriodYear(), source.getPeriodMonth()));
        target.setStatus(source.getStatus());
        target.setPaymentDate(source.getPaymentDate());
        target.setRemarks(source.getRemarks());
        target.setTotalPayslips(source.getPayslips().size());
        target.setApprovedPayslips((int) payslipRepository.countApprovedByPeriod(source.getId()));
    }

    private String buildLabel(int year, int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
    }
}
