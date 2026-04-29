package com.juviai.payroll.service;

import com.juviai.payroll.domain.*;
import com.juviai.payroll.repo.PayrollPeriodRepository;
import com.juviai.payroll.repo.PayslipRepository;
import com.juviai.payroll.service.impl.PayrollPeriodServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollPeriodServiceImplTest {

    @Mock private PayrollPeriodRepository periodRepository;
    @Mock private PayslipRepository       payslipRepository;

    @InjectMocks
    private PayrollPeriodServiceImpl service;

    private UUID          b2bUnitId;
    private UUID          createdBy;
    private PayrollPeriod draft;

    @BeforeEach
    void setUp() {
        b2bUnitId = UUID.randomUUID();
        createdBy = UUID.randomUUID();

        draft = new PayrollPeriod(b2bUnitId, 2026, 4, createdBy);
        setId(draft, UUID.randomUUID());
        draft.setStatus(PayrollPeriodStatus.DRAFT);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Create valid period — saved with DRAFT status")
    void create_valid_savedAsDraft() {
        when(periodRepository.existsByB2bUnitIdAndPeriodYearAndPeriodMonth(b2bUnitId, 2026, 4))
                .thenReturn(false);
        when(periodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayrollPeriod period = service.create(b2bUnitId, 2026, 4, createdBy);

        assertThat(period.getStatus()).isEqualTo(PayrollPeriodStatus.DRAFT);
        assertThat(period.getPeriodYear()).isEqualTo(2026);
        assertThat(period.getPeriodMonth()).isEqualTo(4);
        assertThat(period.getB2bUnitId()).isEqualTo(b2bUnitId);
    }

    @Test
    @DisplayName("Create duplicate period — throws IllegalStateException")
    void create_duplicate_throws() {
        when(periodRepository.existsByB2bUnitIdAndPeriodYearAndPeriodMonth(b2bUnitId, 2026, 4))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(b2bUnitId, 2026, 4, createdBy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Create with invalid month 13 — throws")
    void create_invalidMonth_throws() {
        assertThatThrownBy(() -> service.create(b2bUnitId, 2026, 13, createdBy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("month");
    }

    @Test
    @DisplayName("Create with null b2bUnitId — throws")
    void create_nullOrg_throws() {
        assertThatThrownBy(() -> service.create(null, 2026, 4, createdBy))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("b2bUnitId");
    }

    // ── startProcessing ───────────────────────────────────────────────────────

    @Test
    @DisplayName("startProcessing on DRAFT period — transitions to PROCESSING")
    void startProcessing_draft_transitions() {
        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(periodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayrollPeriod result = service.startProcessing(draft.getId(), createdBy);

        assertThat(result.getStatus()).isEqualTo(PayrollPeriodStatus.PROCESSING);
    }

    @Test
    @DisplayName("startProcessing on non-DRAFT period — throws")
    void startProcessing_nonDraft_throws() {
        draft.setStatus(PayrollPeriodStatus.PROCESSING);
        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.startProcessing(draft.getId(), createdBy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PROCESSING");
    }

    // ── finalize ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Finalize PROCESSING period with all APPROVED payslips — transitions to FINALIZED")
    void finalize_allApproved_transitions() {
        draft.setStatus(PayrollPeriodStatus.PROCESSING);

        Payslip approved = new Payslip();
        approved.setStatus(PayslipStatus.APPROVED);

        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(payslipRepository.findByPayrollPeriodIdOrderByEmployeeId(draft.getId()))
                .thenReturn(List.of(approved));
        when(periodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayrollPeriod result = service.finalize(draft.getId(), createdBy);

        assertThat(result.getStatus()).isEqualTo(PayrollPeriodStatus.FINALIZED);
    }

    @Test
    @DisplayName("Finalize with DRAFT payslips present — throws")
    void finalize_draftPayslipPresent_throws() {
        draft.setStatus(PayrollPeriodStatus.PROCESSING);

        Payslip draftPayslip = new Payslip();
        draftPayslip.setStatus(PayslipStatus.DRAFT);

        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(payslipRepository.findByPayrollPeriodIdOrderByEmployeeId(draft.getId()))
                .thenReturn(List.of(draftPayslip));

        assertThatThrownBy(() -> service.finalize(draft.getId(), createdBy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("Finalize non-PROCESSING period — throws")
    void finalize_nonProcessing_throws() {
        draft.setStatus(PayrollPeriodStatus.DRAFT); // not PROCESSING
        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.finalize(draft.getId(), createdBy))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── markPaid ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("markPaid on FINALIZED period — transitions to PAID and records payment date")
    void markPaid_finalized_transitions() {
        draft.setStatus(PayrollPeriodStatus.FINALIZED);
        LocalDate paymentDate = LocalDate.of(2026, 4, 30);

        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(payslipRepository.findByPayrollPeriodIdAndStatus(draft.getId(), PayslipStatus.APPROVED))
                .thenReturn(List.of());
        when(periodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayrollPeriod result = service.markPaid(draft.getId(), paymentDate, "April 2026 payroll");

        assertThat(result.getStatus()).isEqualTo(PayrollPeriodStatus.PAID);
        assertThat(result.getPaymentDate()).isEqualTo(paymentDate);
    }

    @Test
    @DisplayName("markPaid with null paymentDate — throws")
    void markPaid_nullDate_throws() {
        draft.setStatus(PayrollPeriodStatus.FINALIZED);
        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.markPaid(draft.getId(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentDate");
    }

    @Test
    @DisplayName("markPaid on non-FINALIZED period — throws")
    void markPaid_nonFinalized_throws() {
        draft.setStatus(PayrollPeriodStatus.PROCESSING);
        when(periodRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.markPaid(draft.getId(), LocalDate.now(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById with unknown id — throws EntityNotFoundException")
    void getById_unknown_throws() {
        UUID unknown = UUID.randomUUID();
        when(periodRepository.findById(unknown)).thenReturn(Optional.empty());

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
            throw new RuntimeException(e);
        }
    }
}
