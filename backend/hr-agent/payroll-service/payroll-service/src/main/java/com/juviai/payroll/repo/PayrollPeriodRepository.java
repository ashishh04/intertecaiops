package com.juviai.payroll.repo;

import com.juviai.payroll.domain.PayrollPeriod;
import com.juviai.payroll.domain.PayrollPeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, UUID> {

    Optional<PayrollPeriod> findByB2bUnitIdAndPeriodYearAndPeriodMonth(
            UUID b2bUnitId, int year, int month);

    List<PayrollPeriod> findByB2bUnitIdOrderByPeriodYearDescPeriodMonthDesc(UUID b2bUnitId);

    List<PayrollPeriod> findByB2bUnitIdAndStatusOrderByPeriodYearDescPeriodMonthDesc(
            UUID b2bUnitId, PayrollPeriodStatus status);

    boolean existsByB2bUnitIdAndPeriodYearAndPeriodMonth(UUID b2bUnitId, int year, int month);
}
