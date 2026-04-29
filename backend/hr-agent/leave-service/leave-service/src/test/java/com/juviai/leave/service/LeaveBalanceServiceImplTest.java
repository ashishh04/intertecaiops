package com.juviai.leave.service;

import com.juviai.leave.domain.*;
import com.juviai.leave.repo.*;
import com.juviai.leave.service.impl.LeaveBalanceServiceImpl;
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
class LeaveBalanceServiceImplTest {

    @Mock private LeaveBalanceRepository  balanceRepository;
    @Mock private LeaveTypeRepository     leaveTypeRepository;
    @Mock private LeavePolicyRepository   policyRepository;

    @InjectMocks
    private LeaveBalanceServiceImpl service;

    private UUID       employeeId;
    private UUID       b2bUnitId;
    private LeaveType  casualLeave;
    private LeaveBalance balance;

    @BeforeEach
    void setUp() {
        employeeId  = UUID.randomUUID();
        b2bUnitId   = UUID.randomUUID();

        casualLeave = new LeaveType();
        setId(casualLeave, UUID.randomUUID());
        casualLeave.setB2bUnitId(b2bUnitId);
        casualLeave.setCode("CL");
        casualLeave.setName("Casual Leave");
        casualLeave.setPaid(true);
        casualLeave.setCarryForwardAllowed(false);
        casualLeave.setMaxCarryForwardDays(0);

        balance = new LeaveBalance(employeeId, casualLeave, 2026, new BigDecimal("12"));
        setId(balance, UUID.randomUUID());
    }

    // ── availableDays computed property ───────────────────────────────────────

    @Test
    @DisplayName("availableDays = allocated + carried_forward - used - pending")
    void availableDays_computed_correctly() {
        balance.setAllocatedDays(new BigDecimal("12"));
        balance.setCarriedForward(new BigDecimal("3"));
        balance.setUsedDays(new BigDecimal("5"));
        balance.setPendingDays(new BigDecimal("2"));

        BigDecimal available = balance.getAvailableDays();
        // 12 + 3 - 5 - 2 = 8
        assertThat(available).isEqualByComparingTo(new BigDecimal("8"));
    }

    @Test
    @DisplayName("availableDays never goes below zero")
    void availableDays_neverNegative() {
        balance.setAllocatedDays(new BigDecimal("5"));
        balance.setUsedDays(new BigDecimal("5"));
        balance.setPendingDays(new BigDecimal("2"));

        assertThat(balance.getAvailableDays()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── credit ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("credit adds to allocatedDays")
    void credit_addsToAllocated() {
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BigDecimal original = balance.getAllocatedDays();
        service.credit(employeeId, casualLeave.getId(), 2026, new BigDecimal("2"));

        assertThat(balance.getAllocatedDays())
                .isEqualByComparingTo(original.add(new BigDecimal("2")));
    }

    // ── reserve ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reserve within available balance — adds to pendingDays")
    void reserve_withinBalance_addsToPending() {
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.reserve(employeeId, casualLeave.getId(), 2026, new BigDecimal("3"));

        assertThat(balance.getPendingDays()).isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    @DisplayName("reserve exceeding available paid balance — throws")
    void reserve_exceedsAvailable_throws() {
        balance.setAllocatedDays(new BigDecimal("2")); // only 2 days available

        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));

        assertThatThrownBy(() ->
                service.reserve(employeeId, casualLeave.getId(), 2026, new BigDecimal("5")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient");
    }

    @Test
    @DisplayName("reserve exceeding available for unpaid leave — allowed (LOP)")
    void reserve_exceedsAvailable_unpaidLeave_allowed() {
        casualLeave.setPaid(false);  // LOP leave type
        balance.setAllocatedDays(BigDecimal.ZERO);

        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Should not throw — LOP is allowed when balance is zero
        assertThatNoException().isThrownBy(() ->
                service.reserve(employeeId, casualLeave.getId(), 2026, new BigDecimal("3")));
    }

    // ── release ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("release reduces pendingDays, not below zero")
    void release_reducesPending() {
        balance.setPendingDays(new BigDecimal("3"));

        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.release(employeeId, casualLeave.getId(), 2026, new BigDecimal("3"));

        assertThat(balance.getPendingDays()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── consume ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("consume moves days from pending to used")
    void consume_movesFromPendingToUsed() {
        balance.setPendingDays(new BigDecimal("3"));
        balance.setUsedDays(new BigDecimal("2"));

        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.consume(employeeId, casualLeave.getId(), 2026, new BigDecimal("3"));

        assertThat(balance.getPendingDays()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("5"));
    }

    // ── addLop ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addLop increments lopDays")
    void addLop_incrementsLopDays() {
        balance.setLopDays(new BigDecimal("1"));

        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, casualLeave.getId(), 2026))
                .thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addLop(employeeId, casualLeave.getId(), 2026, new BigDecimal("2"));

        assertThat(balance.getLopDays()).isEqualByComparingTo(new BigDecimal("3"));
    }

    // ── initializeForEmployee ─────────────────────────────────────────────────

    @Test
    @DisplayName("initializeForEmployee creates UPFRONT balances for all active policies")
    void initializeForEmployee_createsUpfrontBalances() {
        LeavePolicy policy = buildPolicy(casualLeave, AccrualType.UPFRONT, new BigDecimal("12"));

        when(policyRepository.findAllActivePoliciesForOrg(eq(b2bUnitId), any()))
                .thenReturn(List.of(policy));
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), eq(2026)))
                .thenReturn(Optional.empty());
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<LeaveBalance> created = service.initializeForEmployee(employeeId, b2bUnitId, 2026);

        assertThat(created).hasSize(1);
        assertThat(created.get(0).getAllocatedDays()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(created.get(0).getEmployeeId()).isEqualTo(employeeId);
    }

    @Test
    @DisplayName("initializeForEmployee skips MONTHLY accrual types")
    void initializeForEmployee_skipsMONTHLY() {
        LeavePolicy policy = buildPolicy(casualLeave, AccrualType.MONTHLY, new BigDecimal("12"));

        when(policyRepository.findAllActivePoliciesForOrg(eq(b2bUnitId), any()))
                .thenReturn(List.of(policy));

        List<LeaveBalance> created = service.initializeForEmployee(employeeId, b2bUnitId, 2026);

        assertThat(created).isEmpty();
        verify(balanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("initializeForEmployee is idempotent — does not create duplicate")
    void initializeForEmployee_idempotent_noDuplicate() {
        LeavePolicy policy = buildPolicy(casualLeave, AccrualType.UPFRONT, new BigDecimal("12"));

        when(policyRepository.findAllActivePoliciesForOrg(eq(b2bUnitId), any()))
                .thenReturn(List.of(policy));
        when(balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(any(), any(), eq(2026)))
                .thenReturn(Optional.of(balance)); // already exists

        List<LeaveBalance> created = service.initializeForEmployee(employeeId, b2bUnitId, 2026);

        assertThat(created).isEmpty();
        verify(balanceRepository, never()).save(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private LeavePolicy buildPolicy(LeaveType lt, AccrualType accrualType, BigDecimal daysPerYear) {
        LeavePolicy p = new LeavePolicy();
        p.setLeaveType(lt);
        p.setB2bUnitId(b2bUnitId);
        p.setAccrualType(accrualType);
        p.setDaysPerYear(daysPerYear);
        p.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        p.setActive(true);
        return p;
    }

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
