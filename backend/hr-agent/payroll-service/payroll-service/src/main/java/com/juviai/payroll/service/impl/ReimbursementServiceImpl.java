package com.juviai.payroll.service.impl;

import com.juviai.payroll.domain.PayrollPeriod;
import com.juviai.payroll.domain.Reimbursement;
import com.juviai.payroll.domain.ReimbursementStatus;
import com.juviai.payroll.repo.PayrollPeriodRepository;
import com.juviai.payroll.repo.ReimbursementRepository;
import com.juviai.payroll.service.ReimbursementService;
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
public class ReimbursementServiceImpl implements ReimbursementService {

    private final ReimbursementRepository repository;
    private final PayrollPeriodRepository  periodRepository;

    @Override
    @Transactional
    public Reimbursement submit(Reimbursement reimbursement) {
        if (reimbursement.getEmployeeId() == null)
            throw new IllegalArgumentException("employeeId is required");
        if (reimbursement.getClaimAmount() == null
                || reimbursement.getClaimAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("claimAmount must be > 0");
        if (reimbursement.getClaimDate() == null)
            throw new IllegalArgumentException("claimDate is required");
        if (reimbursement.getCategory() == null)
            throw new IllegalArgumentException("category is required");

        reimbursement.setStatus(ReimbursementStatus.PENDING);
        Reimbursement saved = repository.save(reimbursement);
        log.info("Reimbursement {} submitted by employee {} — category: {}, amount: {}",
                saved.getId(), saved.getEmployeeId(), saved.getCategory(), saved.getClaimAmount());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Reimbursement getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reimbursement not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reimbursement> listByEmployee(UUID employeeId) {
        return repository.findByEmployeeIdOrderByClaimDateDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reimbursement> listByEmployeeAndStatus(UUID employeeId, ReimbursementStatus status) {
        return repository.findByEmployeeIdAndStatusOrderByClaimDateDesc(employeeId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reimbursement> listPending() {
        return repository.findByStatusOrderByCreatedAtAsc(ReimbursementStatus.PENDING);
    }

    @Override
    @Transactional
    public Reimbursement approve(UUID id, BigDecimal approvedAmount, UUID approvedBy) {
        Reimbursement r = getById(id);
        if (r.getStatus() != ReimbursementStatus.PENDING) {
            throw new IllegalStateException("Cannot approve: status is " + r.getStatus());
        }
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("approvedAmount must be >= 0");
        if (approvedAmount.compareTo(r.getClaimAmount()) > 0)
            throw new IllegalArgumentException("approvedAmount cannot exceed claimAmount");

        r.setApprovedAmount(approvedAmount);
        r.setStatus(ReimbursementStatus.APPROVED);
        r.setApprovedBy(approvedBy);
        r.setApprovedAt(Instant.now());

        log.info("Reimbursement {} approved: {} (claimed: {}) by {}",
                id, approvedAmount, r.getClaimAmount(), approvedBy);
        return repository.save(r);
    }

    @Override
    @Transactional
    public Reimbursement reject(UUID id, String remarks, UUID rejectedBy) {
        Reimbursement r = getById(id);
        if (r.getStatus() != ReimbursementStatus.PENDING) {
            throw new IllegalStateException("Cannot reject: status is " + r.getStatus());
        }
        r.setStatus(ReimbursementStatus.REJECTED);
        r.setRemarks(remarks);
        r.setApprovedBy(rejectedBy);
        r.setApprovedAt(Instant.now());

        log.info("Reimbursement {} rejected by {}", id, rejectedBy);
        return repository.save(r);
    }

    @Override
    @Transactional
    public List<Reimbursement> settleInPeriod(List<UUID> reimbursementIds, UUID periodId) {
        PayrollPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Payroll period not found: " + periodId));

        List<Reimbursement> toSettle = repository.findAllById(reimbursementIds);
        for (Reimbursement r : toSettle) {
            if (r.getStatus() != ReimbursementStatus.APPROVED) {
                throw new IllegalStateException(
                        "Reimbursement " + r.getId() + " is not APPROVED (status: " + r.getStatus() + ")");
            }
            r.setStatus(ReimbursementStatus.PAID);
            r.setPaidInPeriod(period);
        }

        List<Reimbursement> settled = repository.saveAll(toSettle);
        log.info("Settled {} reimbursements in payroll period {}", settled.size(), periodId);
        return settled;
    }
}
