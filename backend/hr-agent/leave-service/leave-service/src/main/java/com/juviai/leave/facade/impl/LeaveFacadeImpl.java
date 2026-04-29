package com.juviai.leave.facade.impl;

import com.juviai.leave.converter.LeaveConverters.*;
import com.juviai.leave.domain.*;
import com.juviai.leave.dto.LeaveDtos.*;
import com.juviai.leave.facade.LeaveFacade;
import com.juviai.leave.repo.LeavePolicyRepository;
import com.juviai.leave.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveFacadeImpl implements LeaveFacade {

    private final LeaveTypeService    leaveTypeService;
    private final LeaveBalanceService balanceService;
    private final LeaveRequestService requestService;
    private final HolidayService      holidayService;
    private final CompOffService      compOffService;
    private final LeavePolicyRepository policyRepository;

    private final LeaveTypeConverter    leaveTypeConverter;
    private final LeavePolicyConverter  policyConverter;
    private final LeaveBalanceConverter balanceConverter;
    private final LeaveRequestConverter requestConverter;
    private final HolidayConverter      holidayConverter;
    private final CompOffConverter      compOffConverter;

    // ── Leave Types ────────────────────────────────────────────────────────

    @Override
    public LeaveTypeDto createLeaveType(CreateLeaveTypeRequestDto req) {
        LeaveType lt = new LeaveType();
        lt.setB2bUnitId(req.getB2bUnitId());
        lt.setCode(req.getCode().toUpperCase());
        lt.setName(req.getName());
        lt.setDescription(req.getDescription());
        lt.setPaid(req.isPaid());
        lt.setRequiresDocument(req.isRequiresDocument());
        lt.setMaxConsecutiveDays(req.getMaxConsecutiveDays());
        lt.setCarryForwardAllowed(req.isCarryForwardAllowed());
        lt.setMaxCarryForwardDays(req.getMaxCarryForwardDays());
        lt.setEncashable(req.isEncashable());
        return leaveTypeConverter.convert(leaveTypeService.create(lt));
    }

    @Override
    public List<LeaveTypeDto> listLeaveTypes(UUID b2bUnitId) {
        return leaveTypeConverter.convertAll(leaveTypeService.listActive(b2bUnitId));
    }

    // ── Leave Policies ─────────────────────────────────────────────────────

    @Override
    public LeavePolicyDto createPolicy(CreateLeavePolicyRequestDto req) {
        LeaveType leaveType = leaveTypeService.getById(req.getLeaveTypeId());
        LeavePolicy policy = new LeavePolicy();
        policy.setLeaveType(leaveType);
        policy.setB2bUnitId(req.getB2bUnitId());
        policy.setApplicableTo(req.getApplicableTo());
        policy.setGender(req.getGender());
        policy.setDaysPerYear(req.getDaysPerYear());
        policy.setAccrualType(req.getAccrualType());
        policy.setMinTenureDays(req.getMinTenureDays());
        policy.setEffectiveFrom(req.getEffectiveFrom());
        policy.setEffectiveTo(req.getEffectiveTo());
        policy.setActive(true);
        return policyConverter.convert(policyRepository.save(policy));
    }

    @Override
    public List<LeavePolicyDto> listPolicies(UUID b2bUnitId) {
        return policyConverter.convertAll(policyRepository.findByB2bUnitIdAndActiveTrue(b2bUnitId));
    }

    // ── Leave Balances ─────────────────────────────────────────────────────

    @Override
    public List<LeaveBalanceDto> listBalances(UUID employeeId, int year) {
        return balanceConverter.convertAll(balanceService.listBalances(employeeId, year));
    }

    @Override
    public List<LeaveBalanceDto> initializeBalances(UUID employeeId, UUID b2bUnitId, int year) {
        return balanceConverter.convertAll(
                balanceService.initializeForEmployee(employeeId, b2bUnitId, year));
    }

    // ── Leave Requests ─────────────────────────────────────────────────────

    @Override
    public LeaveRequestDto apply(UUID employeeId, ApplyLeaveRequestDto req) {
        LeaveType leaveType = leaveTypeService.getById(req.getLeaveTypeId());
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setLeaveType(leaveType);
        request.setFromDate(req.getFromDate());
        request.setToDate(req.getToDate());
        request.setHalfDay(req.isHalfDay());
        request.setHalfDayPeriod(req.getHalfDayPeriod());
        request.setReason(req.getReason());
        request.setDocumentUrl(req.getDocumentUrl());
        return requestConverter.convert(requestService.apply(request));
    }

    @Override
    public LeaveRequestDto getRequest(UUID id) {
        return requestConverter.convert(requestService.getById(id));
    }

    @Override
    public List<LeaveRequestDto> listMyRequests(UUID employeeId, LeaveRequestStatus status) {
        List<LeaveRequest> list = (status != null)
                ? requestService.listByEmployeeAndStatus(employeeId, status)
                : requestService.listByEmployee(employeeId);
        return requestConverter.convertAll(list);
    }

    @Override
    public List<LeaveRequestDto> listPendingForOrg(UUID b2bUnitId) {
        return requestConverter.convertAll(requestService.listPendingByOrg(b2bUnitId));
    }

    @Override
    public LeaveRequestDto approve(UUID id, UUID approvedBy) {
        return requestConverter.convert(requestService.approve(id, approvedBy));
    }

    @Override
    public LeaveRequestDto reject(UUID id, RejectLeaveRequestDto req, UUID rejectedBy) {
        return requestConverter.convert(requestService.reject(id, req.getReason(), rejectedBy));
    }

    @Override
    public LeaveRequestDto cancel(UUID id, UUID employeeId) {
        return requestConverter.convert(requestService.cancel(id, employeeId));
    }

    @Override
    public LeaveRequestDto revoke(UUID id, RevokeLeaveRequestDto req, UUID revokedBy) {
        return requestConverter.convert(requestService.revoke(id, req.getReason(), revokedBy));
    }

    // ── Holidays ───────────────────────────────────────────────────────────

    @Override
    public HolidayDto createHoliday(CreateHolidayRequestDto req) {
        PublicHoliday holiday = new PublicHoliday();
        holiday.setB2bUnitId(req.getB2bUnitId());
        holiday.setHolidayDate(req.getHolidayDate());
        holiday.setName(req.getName());
        holiday.setHolidayType(req.getHolidayType());
        holiday.setRegion(req.getRegion());
        return holidayConverter.convert(holidayService.create(holiday));
    }

    @Override
    public List<HolidayDto> listHolidays(UUID b2bUnitId, int year) {
        return holidayConverter.convertAll(holidayService.listByOrg(b2bUnitId, year));
    }

    @Override
    public void deleteHoliday(UUID id) {
        holidayService.delete(id);
    }

    // ── Comp Offs ──────────────────────────────────────────────────────────

    @Override
    public CompOffDto requestCompOff(UUID employeeId, RequestCompOffDto req) {
        CompOff compOff = new CompOff();
        compOff.setEmployeeId(employeeId);
        compOff.setWorkedDate(req.getWorkedDate());
        compOff.setReason(req.getReason());
        compOff.setCredits(req.getCredits());
        return compOffConverter.convert(compOffService.request(compOff));
    }

    @Override
    public List<CompOffDto> listMyCompOffs(UUID employeeId) {
        return compOffConverter.convertAll(compOffService.listByEmployee(employeeId));
    }

    @Override
    public List<CompOffDto> listPendingCompOffs() {
        return compOffConverter.convertAll(compOffService.listPending());
    }

    @Override
    public CompOffDto approveCompOff(UUID id, UUID approvedBy) {
        return compOffConverter.convert(compOffService.approve(id, approvedBy));
    }

    @Override
    public CompOffDto rejectCompOff(UUID id, UUID rejectedBy) {
        return compOffConverter.convert(compOffService.reject(id, rejectedBy));
    }

    // ── Payroll integration ────────────────────────────────────────────────

    @Override
    public LopSummaryDto getLopSummary(UUID employeeId, int year, int month) {
        LopSummaryDto dto = new LopSummaryDto();
        dto.setEmployeeId(employeeId);
        dto.setYear(year);
        dto.setMonth(month);
        dto.setLopDays(requestService.getLopDays(employeeId, year, month));
        return dto;
    }
}
