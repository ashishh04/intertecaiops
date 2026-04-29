package com.juviai.user.organisation.facade.impl;

import com.juviai.user.organisation.converter.AddressConverter;
import com.juviai.user.organisation.converter.B2BUnitConverter;
import com.juviai.user.organisation.converter.HolidayCalendarConverter;
import com.juviai.user.organisation.converter.HolidayConverter;
import com.juviai.user.organisation.facade.B2BUnitFacade;
import com.juviai.user.organisation.service.B2BUnitService;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.organisation.web.dto.B2BUnitDTO;
import com.juviai.user.organisation.web.dto.CreateHolidayCalendarRequest;
import com.juviai.user.organisation.web.dto.CreateHolidayRequest;
import com.juviai.user.organisation.web.dto.HolidayCalendarDTO;
import com.juviai.user.organisation.web.dto.HolidayDTO;
import com.juviai.user.organisation.web.dto.OnboardRequest;
import com.juviai.user.organisation.web.dto.UpdateCompanyCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class B2BUnitFacadeImpl implements B2BUnitFacade {

    private final B2BUnitService b2bUnitService;
    private final B2BUnitConverter b2bUnitConverter;
    private final AddressConverter addressConverter;
    private final HolidayCalendarConverter holidayCalendarConverter;
    private final HolidayConverter holidayConverter;

    @Override
    public void addAdmin(UUID b2bUnitId, UUID userId, String email) {
        b2bUnitService.addAdmin(b2bUnitId, userId, email);
    }

    @Override
    public B2BUnitDTO selfOnboard(OnboardRequest request) {
        return b2bUnitConverter.convert(b2bUnitService.selfSignup(request));
    }

    @Override
    public B2BUnitDTO approve(UUID id, String approver) {
        return b2bUnitConverter.convert(b2bUnitService.approve(id, approver));
    }

    @Override
    public Page<B2BUnitDTO> pending(Pageable pageable) {
        return b2bUnitService.listPending(pageable).map(b2bUnitConverter::convert);
    }

    @Override
    public Page<B2BUnitDTO> search(String q, Pageable pageable) {
        return b2bUnitService.searchByName(q, pageable).map(b2bUnitConverter::convert);
    }

    @Override
    public Page<B2BUnitDTO> adminList(String q, Pageable pageable) {
        return b2bUnitService.adminList(q, pageable).map(b2bUnitConverter::convert);
    }

    @Override
    public B2BUnitDTO getById(UUID id) {
        return b2bUnitConverter.convert(b2bUnitService.findById(id));
    }

    @Override
    public B2BUnitDTO updateCompanyCode(UUID id, UpdateCompanyCodeRequest request) {
        if (request == null || request.getCompanyCode() == null || request.getCompanyCode().isBlank()) {
            throw new IllegalArgumentException("companyCode is required");
        }
        return b2bUnitConverter.convert(b2bUnitService.updateCompanyCode(id, request));
    }

    @Override
    public Page<AddressDTO> getAddresses(UUID id, Pageable pageable) {
        return b2bUnitService.getAddresses(id, pageable).map(addressConverter::convert);
    }

    @Override
    public Page<HolidayCalendarDTO> getHolidayCalendars(UUID id, Pageable pageable) {
        return b2bUnitService.getHolidayCalendars(id, pageable).map(holidayCalendarConverter::convert);
    }

    @Override
    public HolidayCalendarDTO createHolidayCalendar(UUID id, CreateHolidayCalendarRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        return holidayCalendarConverter.convert(b2bUnitService.createHolidayCalendar(id, request));
    }

    @Override
    public HolidayCalendarDTO getHolidayCalendar(UUID b2bUnitId, UUID calendarId) {
        return holidayCalendarConverter.convert(b2bUnitService.getHolidayCalendar(b2bUnitId, calendarId));
    }

    @Override
    public HolidayDTO createHoliday(UUID b2bUnitId, UUID calendarId, CreateHolidayRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        return holidayConverter.convert(b2bUnitService.createHoliday(b2bUnitId, calendarId, request));
    }

    @Override
    public Page<HolidayDTO> getHolidays(UUID b2bUnitId, UUID calendarId, Pageable pageable) {
        return b2bUnitService.getHolidays(b2bUnitId, calendarId, pageable).map(holidayConverter::convert);
    }

    @Override
    public void deleteHoliday(UUID b2bUnitId, UUID calendarId, UUID holidayId) {
        b2bUnitService.deleteHoliday(b2bUnitId, calendarId, holidayId);
    }

    @Override
    public B2BUnitDTO addAddress(UUID id, AddressDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        return b2bUnitConverter.convert(b2bUnitService.addAddress(id, request));
    }
}
