package com.juviai.leave.converter;

import com.juviai.leave.domain.*;
import com.juviai.leave.dto.LeaveDtos.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

public class LeaveConverters {

// ── LeaveTypeConverter ────────────────────────────────────────────────────────

@Component
public static class LeaveTypeConverter extends AbstractPopulatingConverter<LeaveType, LeaveTypeDto> {
    @Override protected LeaveTypeDto createTarget() { return new LeaveTypeDto(); }
    @Override protected void populate(LeaveType s, LeaveTypeDto t) {
        t.setId(s.getId());
        t.setB2bUnitId(s.getB2bUnitId());
        t.setCode(s.getCode());
        t.setName(s.getName());
        t.setDescription(s.getDescription());
        t.setPaid(s.isPaid());
        t.setRequiresDocument(s.isRequiresDocument());
        t.setMaxConsecutiveDays(s.getMaxConsecutiveDays());
        t.setCarryForwardAllowed(s.isCarryForwardAllowed());
        t.setMaxCarryForwardDays(s.getMaxCarryForwardDays());
        t.setEncashable(s.isEncashable());
        t.setActive(s.isActive());
    }
}

// ── LeavePolicyConverter ──────────────────────────────────────────────────────

@Component
public static class LeavePolicyConverter extends AbstractPopulatingConverter<LeavePolicy, LeavePolicyDto> {
    @Override protected LeavePolicyDto createTarget() { return new LeavePolicyDto(); }
    @Override protected void populate(LeavePolicy s, LeavePolicyDto t) {
        t.setId(s.getId());
        t.setLeaveTypeId(s.getLeaveType().getId());
        t.setLeaveTypeCode(s.getLeaveType().getCode());
        t.setB2bUnitId(s.getB2bUnitId());
        t.setApplicableTo(s.getApplicableTo());
        t.setGender(s.getGender());
        t.setDaysPerYear(s.getDaysPerYear());
        t.setAccrualType(s.getAccrualType());
        t.setMinTenureDays(s.getMinTenureDays());
        t.setEffectiveFrom(s.getEffectiveFrom());
        t.setEffectiveTo(s.getEffectiveTo());
        t.setActive(s.isActive());
    }
}

// ── LeaveBalanceConverter ─────────────────────────────────────────────────────

@Component
public static class LeaveBalanceConverter extends AbstractPopulatingConverter<LeaveBalance, LeaveBalanceDto> {
    @Override protected LeaveBalanceDto createTarget() { return new LeaveBalanceDto(); }
    @Override protected void populate(LeaveBalance s, LeaveBalanceDto t) {
        t.setId(s.getId());
        t.setEmployeeId(s.getEmployeeId());
        t.setLeaveTypeId(s.getLeaveType().getId());
        t.setLeaveTypeCode(s.getLeaveType().getCode());
        t.setLeaveTypeName(s.getLeaveType().getName());
        t.setYear(s.getYear());
        t.setAllocatedDays(s.getAllocatedDays());
        t.setUsedDays(s.getUsedDays());
        t.setPendingDays(s.getPendingDays());
        t.setCarriedForward(s.getCarriedForward());
        t.setLopDays(s.getLopDays());
        t.setAvailableDays(s.getAvailableDays());
    }
}

// ── LeaveRequestConverter ─────────────────────────────────────────────────────

@Component
public static class LeaveRequestConverter extends AbstractPopulatingConverter<LeaveRequest, LeaveRequestDto> {
    @Override protected LeaveRequestDto createTarget() { return new LeaveRequestDto(); }
    @Override protected void populate(LeaveRequest s, LeaveRequestDto t) {
        t.setId(s.getId());
        t.setEmployeeId(s.getEmployeeId());
        t.setLeaveTypeId(s.getLeaveType().getId());
        t.setLeaveTypeCode(s.getLeaveType().getCode());
        t.setLeaveTypeName(s.getLeaveType().getName());
        t.setFromDate(s.getFromDate());
        t.setToDate(s.getToDate());
        t.setTotalDays(s.getTotalDays());
        t.setHalfDay(s.isHalfDay());
        t.setHalfDayPeriod(s.getHalfDayPeriod());
        t.setReason(s.getReason());
        t.setDocumentUrl(s.getDocumentUrl());
        t.setStatus(s.getStatus());
        t.setRejectionReason(s.getRejectionReason());
        t.setDays(s.getDays().stream().map(d -> {
            LeaveRequestDayDto dto = new LeaveRequestDayDto();
            dto.setLeaveDate(d.getLeaveDate());
            dto.setDayFraction(d.getDayFraction());
            return dto;
        }).collect(Collectors.toList()));
    }
}

// ── HolidayConverter ──────────────────────────────────────────────────────────

@Component
public static class HolidayConverter extends AbstractPopulatingConverter<PublicHoliday, HolidayDto> {
    @Override protected HolidayDto createTarget() { return new HolidayDto(); }
    @Override protected void populate(PublicHoliday s, HolidayDto t) {
        t.setId(s.getId());
        t.setB2bUnitId(s.getB2bUnitId());
        t.setHolidayDate(s.getHolidayDate());
        t.setName(s.getName());
        t.setHolidayType(s.getHolidayType());
        t.setRegion(s.getRegion());
    }
}

// ── CompOffConverter ──────────────────────────────────────────────────────────

@Component
public static class CompOffConverter extends AbstractPopulatingConverter<CompOff, CompOffDto> {
    @Override protected CompOffDto createTarget() { return new CompOffDto(); }
    @Override protected void populate(CompOff s, CompOffDto t) {
        t.setId(s.getId());
        t.setEmployeeId(s.getEmployeeId());
        t.setWorkedDate(s.getWorkedDate());
        t.setReason(s.getReason());
        t.setCredits(s.getCredits());
        t.setStatus(s.getStatus());
        t.setExpiresAt(s.getExpiresAt());
    }
}
}
