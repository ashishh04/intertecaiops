package com.juviai.payroll.service;

import com.juviai.payroll.domain.*;
import com.juviai.payroll.repo.PayrollPeriodRepository;
import com.juviai.payroll.repo.ReimbursementRepository;
import com.juviai.payroll.service.impl.ReimbursementServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReimbursementServiceImplTest {

    @Mock private ReimbursementRepository  repository;
    @Mock private PayrollPeriodRepository  periodRepository;

    @InjectMocks
    private ReimbursementServiceImpl service;

    private UUID         employeeId;
    private Reimbursement pending;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();

        pending = new Reimbursement();
        pending.setEmployeeId(employeeId);
        pending.setCategory(ReimbursementCategory.TRAVEL);
        pending.setDescription("Flight to Bangalore for client meeting");
        pending.setClaimAmount(new BigDecimal("8500"));
        pending.setClaimDate(LocalDate.now().minusDays(3));
        pending.setStatus(ReimbursementStatus.PENDING);
        setId(pending, UUID.randomUUID());
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Submit valid claim — status becomes PENDING")
    void submit_valid_setsPending() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reimbursement saved = service.submit(pending);

        assertThat(saved.getStatus()).isEqualTo(ReimbursementStatus.PENDING);
        verify(repository).save(pending);
    }

    @Test
    @DisplayName("Submit with null employeeId — throws")
    void submit_nullEmployee_throws() {
        pending.setEmployeeId(null);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("employeeId");
    }

    @Test
    @DisplayName("Submit with zero claimAmount — throws")
    void submit_zeroAmount_throws() {
        pending.setClaimAmount(BigDecimal.ZERO);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("claimAmount");
    }

    @Test
    @DisplayName("Submit with null claimDate — throws")
    void submit_nullDate_throws() {
        pending.setClaimDate(null);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("claimDate");
    }

    @Test
    @DisplayName("Submit with null category — throws")
    void submit_nullCategory_throws() {
        pending.setCategory(null);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("category");
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Approve with partial amount — status becomes APPROVED")
    void approve_partial_setsApproved() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal partialAmt = new BigDecimal("7000");
        Reimbursement approved = service.approve(pending.getId(), partialAmt, UUID.randomUUID());

        assertThat(approved.getStatus()).isEqualTo(ReimbursementStatus.APPROVED);
        assertThat(approved.getApprovedAmount()).isEqualByComparingTo(partialAmt);
        assertThat(approved.getApprovedBy()).isNotNull();
        assertThat(approved.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("Approve with full amount — status becomes APPROVED")
    void approve_full_setsApproved() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reimbursement approved = service.approve(
                pending.getId(), pending.getClaimAmount(), UUID.randomUUID());

        assertThat(approved.getApprovedAmount())
                .isEqualByComparingTo(pending.getClaimAmount());
    }

    @Test
    @DisplayName("Approve amount exceeding claim — throws")
    void approve_exceedsClaim_throws() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() ->
                service.approve(pending.getId(), new BigDecimal("99999"), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("Approve non-PENDING reimbursement — throws")
    void approve_nonPending_throws() {
        pending.setStatus(ReimbursementStatus.APPROVED);
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() ->
                service.approve(pending.getId(), new BigDecimal("5000"), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Reject PENDING reimbursement — status becomes REJECTED")
    void reject_pending_setsRejected() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reimbursement rejected = service.reject(
                pending.getId(), "Receipt not valid", UUID.randomUUID());

        assertThat(rejected.getStatus()).isEqualTo(ReimbursementStatus.REJECTED);
        assertThat(rejected.getRemarks()).isEqualTo("Receipt not valid");
    }

    @Test
    @DisplayName("Reject non-PENDING reimbursement — throws")
    void reject_nonPending_throws() {
        pending.setStatus(ReimbursementStatus.REJECTED);
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() ->
                service.reject(pending.getId(), "duplicate", UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── Settle ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Settle approved reimbursements in a period — status becomes PAID")
    void settleInPeriod_approved_setsPaid() {
        pending.setStatus(ReimbursementStatus.APPROVED);
        pending.setApprovedAmount(new BigDecimal("7000"));

        PayrollPeriod period = new PayrollPeriod(UUID.randomUUID(), 2026, 4, UUID.randomUUID());
        setId(period, UUID.randomUUID());

        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(repository.findAllById(List.of(pending.getId()))).thenReturn(List.of(pending));
        when(repository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Reimbursement> settled = service.settleInPeriod(
                List.of(pending.getId()), period.getId());

        assertThat(settled).hasSize(1);
        assertThat(settled.get(0).getStatus()).isEqualTo(ReimbursementStatus.PAID);
        assertThat(settled.get(0).getPaidInPeriod()).isEqualTo(period);
    }

    @Test
    @DisplayName("Settle non-APPROVED reimbursement — throws")
    void settleInPeriod_nonApproved_throws() {
        // pending.status is still PENDING
        PayrollPeriod period = new PayrollPeriod(UUID.randomUUID(), 2026, 4, UUID.randomUUID());
        setId(period, UUID.randomUUID());

        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(repository.findAllById(any())).thenReturn(List.of(pending));

        assertThatThrownBy(() ->
                service.settleInPeriod(List.of(pending.getId()), period.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not APPROVED");
    }

    @Test
    @DisplayName("Settle with unknown period id — throws EntityNotFoundException")
    void settleInPeriod_unknownPeriod_throws() {
        UUID unknownPeriod = UUID.randomUUID();
        when(periodRepository.findById(unknownPeriod)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.settleInPeriod(List.of(pending.getId()), unknownPeriod))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById with unknown id — throws EntityNotFoundException")
    void getById_unknown_throws() {
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(unknown))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void setId(Object entity, UUID id) {
        try {
            var f = entity.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            try {
                var f = entity.getClass().getDeclaredField("id");
                f.setAccessible(true);
                f.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
