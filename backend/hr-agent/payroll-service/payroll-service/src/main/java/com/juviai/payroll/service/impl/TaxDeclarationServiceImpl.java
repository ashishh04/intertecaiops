package com.juviai.payroll.service.impl;

import com.juviai.payroll.domain.TaxDeclaration;
import com.juviai.payroll.domain.TaxDeclarationStatus;
import com.juviai.payroll.repo.TaxDeclarationRepository;
import com.juviai.payroll.service.TaxDeclarationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxDeclarationServiceImpl implements TaxDeclarationService {

    private final TaxDeclarationRepository repository;

    @Override
    @Transactional
    public TaxDeclaration submit(TaxDeclaration declaration) {
        if (declaration.getEmployeeId() == null)
            throw new IllegalArgumentException("employeeId is required");
        if (declaration.getFinancialYear() == null || declaration.getFinancialYear().isBlank())
            throw new IllegalArgumentException("financialYear is required (e.g. 2025-26)");
        if (declaration.getDeclaredAmount() == null || declaration.getDeclaredAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("declaredAmount must be > 0");

        declaration.setStatus(TaxDeclarationStatus.PENDING);
        TaxDeclaration saved = repository.save(declaration);
        log.info("Tax declaration {} submitted by employee {} for FY {}",
                saved.getId(), saved.getEmployeeId(), saved.getFinancialYear());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public TaxDeclaration getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tax declaration not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxDeclaration> listByEmployee(UUID employeeId) {
        return repository.findByEmployeeIdOrderByFinancialYearDescCreatedAtDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxDeclaration> listByEmployeeAndYear(UUID employeeId, String financialYear) {
        return repository.findByEmployeeIdAndFinancialYear(employeeId, financialYear);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxDeclaration> listPending() {
        return repository.findByStatusOrderByCreatedAtAsc(TaxDeclarationStatus.PENDING);
    }

    @Override
    @Transactional
    public TaxDeclaration approve(UUID id, BigDecimal approvedAmount, UUID reviewedBy) {
        TaxDeclaration declaration = getById(id);
        if (declaration.getStatus() != TaxDeclarationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot approve: declaration is in status " + declaration.getStatus());
        }
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("approvedAmount must be >= 0");
        if (approvedAmount.compareTo(declaration.getDeclaredAmount()) > 0)
            throw new IllegalArgumentException("approvedAmount cannot exceed declaredAmount");

        declaration.setApprovedAmount(approvedAmount);
        declaration.setStatus(TaxDeclarationStatus.APPROVED);
        declaration.setReviewedBy(reviewedBy);
        declaration.setReviewedAt(Instant.now());

        log.info("Tax declaration {} approved with amount {} by {}", id, approvedAmount, reviewedBy);
        return repository.save(declaration);
    }

    @Override
    @Transactional
    public TaxDeclaration reject(UUID id, UUID reviewedBy) {
        TaxDeclaration declaration = getById(id);
        if (declaration.getStatus() != TaxDeclarationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot reject: declaration is in status " + declaration.getStatus());
        }
        declaration.setStatus(TaxDeclarationStatus.REJECTED);
        declaration.setReviewedBy(reviewedBy);
        declaration.setReviewedAt(Instant.now());

        log.info("Tax declaration {} rejected by {}", id, reviewedBy);
        return repository.save(declaration);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalApprovedAmount(UUID employeeId, String financialYear) {
        return repository.findByEmployeeIdAndFinancialYear(employeeId, financialYear)
                .stream()
                .filter(d -> d.getStatus() == TaxDeclarationStatus.APPROVED)
                .map(TaxDeclaration::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
