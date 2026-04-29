package com.juviai.leave.service.impl;

import com.juviai.leave.domain.*;
import com.juviai.leave.repo.*;
import com.juviai.leave.service.HolidayService;
import com.juviai.leave.service.LeaveBalanceService;
import com.juviai.leave.service.LeaveRequestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository  requestRepository;
    private final LeaveTypeRepository     leaveTypeRepository;
    private final LeaveBalanceService     balanceService;
    private final HolidayService          holidayService;

    // ── Apply ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveRequest apply(LeaveRequest request) {
        validateRequest(request);

        LeaveType leaveType = request.getLeaveType();

        // Overlap check: no duplicate/overlapping open requests
        List<LeaveRequest> overlapping = requestRepository.findOverlapping(
                request.getEmployeeId(), request.getFromDate(), request.getToDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException(
                    "Leave request overlaps with an existing pending or approved request");
        }

        // Count actual working days
        // b2bUnitId is derived from leaveType.b2bUnitId
        BigDecimal workingDays = countWorkingDays(
                leaveType.getB2bUnitId(), request.getFromDate(), request.getToDate());

        if (request.isHalfDay()) {
            workingDays = BigDecimal.valueOf(0.5);
        }

        if (workingDays.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "No working days in the selected date range (all are weekends or holidays)");
        }

        // Max consecutive days policy check
        if (leaveType.getMaxConsecutiveDays() != null
                && workingDays.compareTo(BigDecimal.valueOf(leaveType.getMaxConsecutiveDays())) > 0) {
            throw new IllegalArgumentException(
                    "Request exceeds max consecutive days allowed (" + leaveType.getMaxConsecutiveDays()
                    + ") for leave type " + leaveType.getCode());
        }

        // Document required check
        if (leaveType.isRequiresDocument() && (request.getDocumentUrl() == null || request.getDocumentUrl().isBlank())) {
            throw new IllegalArgumentException(
                    "Document upload is mandatory for leave type: " + leaveType.getCode());
        }

        request.setTotalDays(workingDays);
        request.setStatus(LeaveRequestStatus.PENDING);
        request.setAppliedAt(Instant.now());

        // Build day-level breakdown
        buildDayBreakdown(request, leaveType.getB2bUnitId());

        // Reserve balance (service handles LOP if balance insufficient for paid leave)
        balanceService.reserve(
                request.getEmployeeId(),
                leaveType.getId(),
                request.getFromDate().getYear(),
                workingDays);

        LeaveRequest saved = requestRepository.save(request);
        log.info("Leave request {} submitted: employee {}, type {}, {} days ({} → {})",
                saved.getId(), saved.getEmployeeId(), leaveType.getCode(),
                workingDays, request.getFromDate(), request.getToDate());
        return saved;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public LeaveRequest getById(UUID id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> listByEmployee(UUID employeeId) {
        return requestRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> listByEmployeeAndStatus(UUID employeeId, LeaveRequestStatus status) {
        return requestRepository.findByEmployeeIdAndStatusOrderByAppliedAtDesc(employeeId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> listPendingByOrg(UUID b2bUnitId) {
        return requestRepository.findPendingByOrg(b2bUnitId);
    }

    // ── Status transitions ────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveRequest approve(UUID id, UUID approvedBy) {
        LeaveRequest request = getById(id);
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved. Status: " + request.getStatus());
        }
        request.setStatus(LeaveRequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(Instant.now());

        // Move balance: pending → used
        balanceService.consume(
                request.getEmployeeId(),
                request.getLeaveType().getId(),
                request.getFromDate().getYear(),
                request.getTotalDays());

        log.info("Leave request {} approved by {}", id, approvedBy);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public LeaveRequest reject(UUID id, String reason, UUID rejectedBy) {
        LeaveRequest request = getById(id);
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected. Status: " + request.getStatus());
        }
        request.setStatus(LeaveRequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setApprovedBy(rejectedBy);
        request.setApprovedAt(Instant.now());

        // Release reserved balance
        balanceService.release(
                request.getEmployeeId(),
                request.getLeaveType().getId(),
                request.getFromDate().getYear(),
                request.getTotalDays());

        log.info("Leave request {} rejected by {}, reason: {}", id, rejectedBy, reason);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public LeaveRequest cancel(UUID id, UUID employeeId) {
        LeaveRequest request = getById(id);
        if (!request.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("You can only cancel your own leave requests");
        }
        if (request.getStatus() == LeaveRequestStatus.CANCELLED) {
            throw new IllegalStateException("Request is already cancelled");
        }
        if (request.getStatus() == LeaveRequestStatus.REJECTED
                || request.getStatus() == LeaveRequestStatus.REVOKED) {
            throw new IllegalStateException("Cannot cancel a " + request.getStatus() + " request");
        }
        if (request.getStatus() == LeaveRequestStatus.APPROVED
                && request.getFromDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    "Cannot cancel leave that has already started. Contact HR to revoke.");
        }

        LeaveRequestStatus prev = request.getStatus();
        request.setStatus(LeaveRequestStatus.CANCELLED);

        // Return balance only if it was approved (consumed) or pending (reserved)
        if (prev == LeaveRequestStatus.APPROVED) {
            // add back to allocated
            balanceService.credit(
                    request.getEmployeeId(),
                    request.getLeaveType().getId(),
                    request.getFromDate().getYear(),
                    request.getTotalDays());
            balanceService.consume(
                    request.getEmployeeId(),
                    request.getLeaveType().getId(),
                    request.getFromDate().getYear(),
                    request.getTotalDays().negate().abs()); // reduce used
        } else if (prev == LeaveRequestStatus.PENDING) {
            balanceService.release(
                    request.getEmployeeId(),
                    request.getLeaveType().getId(),
                    request.getFromDate().getYear(),
                    request.getTotalDays());
        }

        log.info("Leave request {} cancelled by employee {}", id, employeeId);
        return requestRepository.save(request);
    }

    @Override
    @Transactional
    public LeaveRequest revoke(UUID id, String reason, UUID revokedBy) {
        LeaveRequest request = getById(id);
        if (request.getStatus() != LeaveRequestStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED requests can be revoked");
        }
        request.setStatus(LeaveRequestStatus.REVOKED);
        request.setRejectionReason(reason);
        request.setApprovedBy(revokedBy);

        // Return the leave days back
        balanceService.credit(
                request.getEmployeeId(),
                request.getLeaveType().getId(),
                request.getFromDate().getYear(),
                request.getTotalDays());

        log.info("Leave request {} revoked by HR {}, reason: {}", id, revokedBy, reason);
        return requestRepository.save(request);
    }

    // ── Payroll integration ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getLopDays(UUID employeeId, int year, int month) {
        LocalDate periodStart = LocalDate.of(year, month, 1);
        LocalDate periodEnd   = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

        List<LeaveRequest> lopRequests = requestRepository
                .findLopRequestsInPeriod(employeeId, periodStart, periodEnd);

        // Sum day fractions within the period month
        BigDecimal total = BigDecimal.ZERO;
        for (LeaveRequest lr : lopRequests) {
            for (var day : lr.getDays()) {
                if (!day.getLeaveDate().isBefore(periodStart)
                        && !day.getLeaveDate().isAfter(periodEnd)) {
                    total = total.add(day.getDayFraction());
                }
            }
        }
        return total;
    }

    // ── Working day calculator ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BigDecimal countWorkingDays(UUID b2bUnitId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new IllegalArgumentException("fromDate must be <= toDate");

        Set<LocalDate> holidays = holidayService.getHolidayDates(b2bUnitId, from, to);

        BigDecimal count = BigDecimal.ZERO;
        LocalDate current = from;
        while (!current.isAfter(to)) {
            DayOfWeek dow = current.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            if (!isWeekend && !holidays.contains(current)) {
                count = count.add(BigDecimal.ONE);
            }
            current = current.plusDays(1);
        }
        return count;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateRequest(LeaveRequest request) {
        if (request.getEmployeeId() == null) throw new IllegalArgumentException("employeeId is required");
        if (request.getLeaveType() == null) throw new IllegalArgumentException("leaveType is required");
        if (request.getFromDate() == null) throw new IllegalArgumentException("fromDate is required");
        if (request.getToDate() == null) throw new IllegalArgumentException("toDate is required");
        if (request.getFromDate().isAfter(request.getToDate()))
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        if (request.getFromDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Cannot apply for past dates");
        if (request.isHalfDay() && !request.getFromDate().equals(request.getToDate()))
            throw new IllegalArgumentException("Half-day leave must be a single day (fromDate = toDate)");
        if (request.isHalfDay() && (request.getHalfDayPeriod() == null || request.getHalfDayPeriod().isBlank()))
            throw new IllegalArgumentException("halfDayPeriod (MORNING/AFTERNOON) is required for half-day leave");
    }

    private void buildDayBreakdown(LeaveRequest request, UUID b2bUnitId) {
        Set<LocalDate> holidays = holidayService.getHolidayDates(
                b2bUnitId, request.getFromDate(), request.getToDate());

        LocalDate current = request.getFromDate();
        while (!current.isAfter(request.getToDate())) {
            DayOfWeek dow = current.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            if (!isWeekend && !holidays.contains(current)) {
                BigDecimal fraction = request.isHalfDay()
                        ? BigDecimal.valueOf(0.5) : BigDecimal.ONE;
                request.getDays().add(new LeaveRequestDay(request, current, fraction));
            }
            current = current.plusDays(1);
        }
    }
}
