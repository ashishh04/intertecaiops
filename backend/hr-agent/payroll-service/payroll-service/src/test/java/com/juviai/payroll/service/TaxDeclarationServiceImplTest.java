package com.juviai.payroll.service;

import com.juviai.payroll.domain.TaxDeclaration;
import com.juviai.payroll.domain.TaxDeclarationStatus;
import com.juviai.payroll.domain.TaxSection;
import com.juviai.payroll.repo.TaxDeclarationRepository;
import com.juviai.payroll.service.impl.TaxDeclarationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxDeclarationServiceImplTest {

    @Mock private TaxDeclarationRepository repository;

    @InjectMocks
    private TaxDeclarationServiceImpl service;

    private UUID employeeId;
    private TaxDeclaration pending;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();

        pending = new TaxDeclaration();
        pending.setEmployeeId(employeeId);
        pending.setSection(TaxSection.SECTION_80C);
        pending.setFinancialYear("2025-26");
        pending.setDescription("PPF contribution");
        pending.setDeclaredAmount(new BigDecimal("150000"));
        pending.setStatus(TaxDeclarationStatus.PENDING);
        setId(pending, UUID.randomUUID());
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Submit valid declaration — status set to PENDING")
    void submit_valid_setsPending() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaxDeclaration saved = service.submit(pending);

        assertThat(saved.getStatus()).isEqualTo(TaxDeclarationStatus.PENDING);
        verify(repository).save(pending);
    }

    @Test
    @DisplayName("Submit with zero amount — throws IllegalArgumentException")
    void submit_zeroAmount_throws() {
        pending.setDeclaredAmount(BigDecimal.ZERO);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("declaredAmount");
    }

    @Test
    @DisplayName("Submit without employeeId — throws")
    void submit_noEmployeeId_throws() {
        pending.setEmployeeId(null);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("employeeId");
    }

    @Test
    @DisplayName("Submit without financialYear — throws")
    void submit_noFinancialYear_throws() {
        pending.setFinancialYear(null);
        assertThatThrownBy(() -> service.submit(pending))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("financialYear");
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Approve with valid amount — status becomes APPROVED")
    void approve_valid_setsApproved() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal approvedAmt = new BigDecimal("100000");
        TaxDeclaration approved = service.approve(pending.getId(), approvedAmt, UUID.randomUUID());

        assertThat(approved.getStatus()).isEqualTo(TaxDeclarationStatus.APPROVED);
        assertThat(approved.getApprovedAmount()).isEqualByComparingTo(approvedAmt);
        assertThat(approved.getReviewedBy()).isNotNull();
        assertThat(approved.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("Approve with amount exceeding declared — throws")
    void approve_exceedsDeclared_throws() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() ->
                service.approve(pending.getId(), new BigDecimal("200000"), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("Approve already-approved declaration — throws")
    void approve_alreadyApproved_throws() {
        pending.setStatus(TaxDeclarationStatus.APPROVED);
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() ->
                service.approve(pending.getId(), new BigDecimal("50000"), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Reject PENDING declaration — status becomes REJECTED")
    void reject_pending_setsRejected() {
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaxDeclaration rejected = service.reject(pending.getId(), UUID.randomUUID());

        assertThat(rejected.getStatus()).isEqualTo(TaxDeclarationStatus.REJECTED);
        assertThat(rejected.getReviewedBy()).isNotNull();
    }

    @Test
    @DisplayName("Reject non-PENDING declaration — throws")
    void reject_nonPending_throws() {
        pending.setStatus(TaxDeclarationStatus.REJECTED);
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.reject(pending.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── totalApprovedAmount ───────────────────────────────────────────────────

    @Test
    @DisplayName("totalApprovedAmount sums only APPROVED declarations")
    void totalApprovedAmount_sumsOnlyApproved() {
        TaxDeclaration approved1 = buildDeclaration(TaxDeclarationStatus.APPROVED, new BigDecimal("50000"), new BigDecimal("50000"));
        TaxDeclaration approved2 = buildDeclaration(TaxDeclarationStatus.APPROVED, new BigDecimal("30000"), new BigDecimal("30000"));
        TaxDeclaration pendingDecl = buildDeclaration(TaxDeclarationStatus.PENDING, new BigDecimal("20000"), null);

        when(repository.findByEmployeeIdAndFinancialYear(employeeId, "2025-26"))
                .thenReturn(List.of(approved1, approved2, pendingDecl));

        BigDecimal total = service.totalApprovedAmount(employeeId, "2025-26");

        assertThat(total).isEqualByComparingTo(new BigDecimal("80000"));
    }

    @Test
    @DisplayName("totalApprovedAmount returns zero when no declarations exist")
    void totalApprovedAmount_noDeclarations_returnsZero() {
        when(repository.findByEmployeeIdAndFinancialYear(employeeId, "2025-26"))
                .thenReturn(List.of());

        assertThat(service.totalApprovedAmount(employeeId, "2025-26"))
                .isEqualByComparingTo(BigDecimal.ZERO);
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

    // ── helpers ───────────────────────────────────────────────────────────────

    private TaxDeclaration buildDeclaration(TaxDeclarationStatus status,
                                             BigDecimal declared, BigDecimal approved) {
        TaxDeclaration d = new TaxDeclaration();
        d.setEmployeeId(employeeId);
        d.setSection(TaxSection.SECTION_80C);
        d.setFinancialYear("2025-26");
        d.setDescription("test");
        d.setDeclaredAmount(declared);
        d.setApprovedAmount(approved);
        d.setStatus(status);
        return d;
    }

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
