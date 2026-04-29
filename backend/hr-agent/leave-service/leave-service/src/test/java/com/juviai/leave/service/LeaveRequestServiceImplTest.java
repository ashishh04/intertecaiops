package com.juviai.leave.service;

import com.juviai.leave.domain.*;
import com.juviai.leave.repo.LeaveRequestRepository;
import com.juviai.leave.repo.LeaveTypeRepository;
import com.juviai.leave.service.impl.LeaveRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock private LeaveRequestRepository requestRepository;
    @Mock private LeaveTypeRepository    leaveTypeRepository;
    @Mock private LeaveBalanceService    balanceService;
    @Mock private HolidayService         holidayService;

    @InjectMocks
    private LeaveRequestServiceImpl service;

    private UUID        employeeId;
    private UUID        b2bUnitId;
    private LeaveType   casualLeave;

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
        casualLeave.setRequiresDocument(false);
        casualLeave.setActive(true);
    }

    @Test
    @DisplayName("Apply for 2 working days — should succeed and reserve balance")
    void apply_validRequest_reservesBalance() {
        // Monday and Tuesday next month
        LocalDate from = LocalDate.now().plusMonths(1).withDayOfMonth(7);  // a Monday
        LocalDate to   = from.plusDays(1);

        LeaveRequest request = buildRequest(from, to);

        when(requestRepository.findOverlapping(employeeId, from, to)).thenReturn(List.of());
        when(holidayService.getHolidayDates(b2bUnitId, from, to)).thenReturn(Set.of());
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = service.apply(request);

        assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.PENDING);
        assertThat(result.getTotalDays()).isGreaterThan(BigDecimal.ZERO);
        verify(balanceService).reserve(eq(employeeId), eq(casualLeave.getId()), eq(from.getYear()), any());
    }

    @Test
    @DisplayName("Apply — overlapping request should throw")
    void apply_overlappingRequest_throws() {
        LocalDate from = LocalDate.now().plusDays(5);
        LocalDate to   = from.plusDays(2);

        LeaveRequest existing = buildRequest(from, to);
        existing.setStatus(LeaveRequestStatus.PENDING);

        when(requestRepository.findOverlapping(employeeId, from, to)).thenReturn(List.of(existing));

        LeaveRequest request = buildRequest(from, to);
        assertThatThrownBy(() -> service.apply(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overlaps");
    }

    @Test
    @DisplayName("Apply for past date should throw")
    void apply_pastDate_throws() {
        LocalDate from = LocalDate.now().minusDays(3);
        LocalDate to   = LocalDate.now().minusDays(1);
        LeaveRequest request = buildRequest(from, to);

        assertThatThrownBy(() -> service.apply(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    @DisplayName("Half-day leave spanning multiple days should throw")
    void apply_halfDayMultipleDays_throws() {
        LocalDate from = LocalDate.now().plusDays(5);
        LocalDate to   = from.plusDays(1);
        LeaveRequest request = buildRequest(from, to);
        request.setHalfDay(true);
        request.setHalfDayPeriod("MORNING");

        assertThatThrownBy(() -> service.apply(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("single day");
    }

    @Test
    @DisplayName("Approve pending request — moves balance from pending to used")
    void approve_pendingRequest_updatesBalance() {
        LeaveRequest request = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        setId(request, UUID.randomUUID());
        request.setStatus(LeaveRequestStatus.PENDING);
        request.setTotalDays(new BigDecimal("2.0"));

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest approved = service.approve(request.getId(), UUID.randomUUID());

        assertThat(approved.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED);
        verify(balanceService).consume(eq(employeeId), eq(casualLeave.getId()),
                anyInt(), eq(new BigDecimal("2.0")));
    }

    @Test
    @DisplayName("Reject pending request — releases reserved balance")
    void reject_pendingRequest_releasesBalance() {
        LeaveRequest request = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        setId(request, UUID.randomUUID());
        request.setStatus(LeaveRequestStatus.PENDING);
        request.setTotalDays(new BigDecimal("2.0"));

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest rejected = service.reject(request.getId(), "Insufficient team coverage", UUID.randomUUID());

        assertThat(rejected.getStatus()).isEqualTo(LeaveRequestStatus.REJECTED);
        verify(balanceService).release(eq(employeeId), eq(casualLeave.getId()),
                anyInt(), eq(new BigDecimal("2.0")));
    }

    @Test
    @DisplayName("countWorkingDays — 5 weekdays with no holidays should return 5")
    void countWorkingDays_noBankHolidays_returnsFive() {
        // Find next Monday
        LocalDate monday = LocalDate.now().plusWeeks(2);
        while (monday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) monday = monday.plusDays(1);
        LocalDate friday = monday.plusDays(4);

        when(holidayService.getHolidayDates(b2bUnitId, monday, friday)).thenReturn(Set.of());

        BigDecimal days = service.countWorkingDays(b2bUnitId, monday, friday);
        assertThat(days).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    @DisplayName("countWorkingDays — 1 holiday in 5 weekdays should return 4")
    void countWorkingDays_withOneHoliday_returnsFour() {
        LocalDate monday = LocalDate.now().plusWeeks(2);
        while (monday.getDayOfWeek() != java.time.DayOfWeek.MONDAY) monday = monday.plusDays(1);
        LocalDate friday = monday.plusDays(4);
        LocalDate wednesday = monday.plusDays(2);

        when(holidayService.getHolidayDates(b2bUnitId, monday, friday))
                .thenReturn(Set.of(wednesday));

        BigDecimal days = service.countWorkingDays(b2bUnitId, monday, friday);
        assertThat(days).isEqualByComparingTo(new BigDecimal("4"));
    }

    @Test
    @DisplayName("Employee cannot cancel another employee's request")
    void cancel_wrongEmployee_throws() {
        LeaveRequest request = buildRequest(
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6));
        setId(request, UUID.randomUUID());
        request.setStatus(LeaveRequestStatus.PENDING);

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        UUID otherEmployee = UUID.randomUUID();
        assertThatThrownBy(() -> service.cancel(request.getId(), otherEmployee))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LeaveRequest buildRequest(LocalDate from, LocalDate to) {
        LeaveRequest r = new LeaveRequest();
        r.setEmployeeId(employeeId);
        r.setLeaveType(casualLeave);
        r.setFromDate(from);
        r.setToDate(to);
        return r;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            try {
                var field = entity.getClass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
