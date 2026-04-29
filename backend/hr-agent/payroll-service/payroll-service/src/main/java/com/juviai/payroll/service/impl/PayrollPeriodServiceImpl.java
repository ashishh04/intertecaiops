package com.juviai.payroll.service.impl;

import com.juviai.payroll.domain.PayrollPeriod;
import com.juviai.payroll.domain.PayrollPeriodStatus;
import com.juviai.payroll.domain.Payslip;
import com.juviai.payroll.domain.PayslipStatus;
import com.juviai.payroll.repo.PayrollPeriodRepository;
import com.juviai.payroll.repo.PayslipRepository;
import com.juviai.payroll.service.PayrollPeriodService;
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
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private final PayrollPeriodRepository periodRepository;
    private final PayslipRepository payslipRepository;

    @Override
    @Transactional
    public PayrollPeriod create(UUID b2bUnitId, int year, int month, UUID createdBy) {
        if (b2bUnitId == null) throw new IllegalArgumentException("b2bUnitId is required");
        if (month < 1 || month > 12) throw new IllegalArgumentException("month must be between 1 and 12");
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Invalid year");

        if (periodRepository.existsByB2bUnitIdAndPeriodYearAndPeriodMonth(b2bUnitId, year, month)) {
            throw new IllegalStateException(
                    "Payroll period already exists for org " + b2bUnitId + " — " + year + "/" + month);
        }

        PayrollPeriod period = new PayrollPeriod(b2bUnitId, year, month, createdBy);
        return periodRepository.save(period);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollPeriod getById(UUID id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPeriod> listByOrg(UUID b2bUnitId) {
        return periodRepository.findByB2bUnitIdOrderByPeriodYearDescPeriodMonthDesc(b2bUnitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPeriod> listByOrgAndStatus(UUID b2bUnitId, PayrollPeriodStatus status) {
        return periodRepository.findByB2bUnitIdAndStatusOrderByPeriodYearDescPeriodMonthDesc(b2bUnitId, status);
    }

    @Override
    @Transactional
    public PayrollPeriod startProcessing(UUID periodId, UUID initiatedBy) {
        PayrollPeriod period = getById(periodId);
        if (period.getStatus() != PayrollPeriodStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot start processing: period is in status " + period.getStatus());
        }
        period.setStatus(PayrollPeriodStatus.PROCESSING);
        log.info("Payroll period {} moved to PROCESSING by {}", periodId, initiatedBy);
        return periodRepository.save(period);
    }

    @Override
    @Transactional
    public PayrollPeriod finalize(UUID periodId, UUID approvedBy) {
        PayrollPeriod period = getById(periodId);
        if (period.getStatus() != PayrollPeriodStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot finalize: period is in status " + period.getStatus());
        }

        // All payslips in this period must be APPROVED before finalization
        List<Payslip> payslips = payslipRepository.findByPayrollPeriodIdOrderByEmployeeId(periodId);
        long draftCount = payslips.stream()
                .filter(p -> p.getStatus() == PayslipStatus.DRAFT)
                .count();
        if (draftCount > 0) {
            throw new IllegalStateException(
                    draftCount + " payslip(s) are still in DRAFT status. Approve all payslips before finalizing.");
        }

        period.setStatus(PayrollPeriodStatus.FINALIZED);
        log.info("Payroll period {} FINALIZED by {}", periodId, approvedBy);
        return periodRepository.save(period);
    }

    @Override
    @Transactional
    public PayrollPeriod markPaid(UUID periodId, LocalDate paymentDate, String remarks) {
        PayrollPeriod period = getById(periodId);
        if (period.getStatus() != PayrollPeriodStatus.FINALIZED) {
            throw new IllegalStateException(
                    "Cannot mark as PAID: period is in status " + period.getStatus());
        }
        if (paymentDate == null) throw new IllegalArgumentException("paymentDate is required");

        period.setStatus(PayrollPeriodStatus.PAID);
        period.setPaymentDate(paymentDate);
        period.setRemarks(remarks);

        // Mark all APPROVED payslips as PAID
        List<Payslip> payslips = payslipRepository.findByPayrollPeriodIdAndStatus(
                periodId, PayslipStatus.APPROVED);
        payslips.forEach(p -> p.setStatus(PayslipStatus.PAID));
        payslipRepository.saveAll(payslips);

        log.info("Payroll period {} marked PAID on {}", periodId, paymentDate);
        return periodRepository.save(period);
    }
}
