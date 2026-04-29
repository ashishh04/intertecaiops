package com.juviai.user.organisation.facade;

import com.juviai.user.organisation.web.dto.B2BUnitDTO;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.organisation.web.dto.CreateHolidayCalendarRequest;
import com.juviai.user.organisation.web.dto.CreateHolidayRequest;
import com.juviai.user.organisation.web.dto.HolidayCalendarDTO;
import com.juviai.user.organisation.web.dto.HolidayDTO;
import com.juviai.user.organisation.web.dto.OnboardRequest;
import com.juviai.user.organisation.web.dto.UpdateCompanyCodeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface B2BUnitFacade {

    /**
     * Add an existing user as a ROLE_BUSINESS_ADMIN for the given B2BUnit.
     * Exactly one of {@code userId} or {@code email} must be provided.
     */
    void addAdmin(UUID b2bUnitId, UUID userId, String email);
    B2BUnitDTO selfOnboard(OnboardRequest request);
    B2BUnitDTO approve(UUID id, String approver);
    Page<B2BUnitDTO> pending(Pageable pageable);
    Page<B2BUnitDTO> search(String q, Pageable pageable);
    Page<B2BUnitDTO> adminList(String q, Pageable pageable);
    B2BUnitDTO getById(UUID id);

    B2BUnitDTO updateCompanyCode(UUID id, UpdateCompanyCodeRequest request);

    Page<AddressDTO> getAddresses(UUID id, Pageable pageable);

    B2BUnitDTO addAddress(UUID id, AddressDTO request);

    Page<HolidayCalendarDTO> getHolidayCalendars(UUID id, Pageable pageable);

    HolidayCalendarDTO createHolidayCalendar(UUID id, CreateHolidayCalendarRequest request);

    HolidayCalendarDTO getHolidayCalendar(UUID b2bUnitId, UUID calendarId);

    HolidayDTO createHoliday(UUID b2bUnitId, UUID calendarId, CreateHolidayRequest request);

    Page<HolidayDTO> getHolidays(UUID b2bUnitId, UUID calendarId, Pageable pageable);

    void deleteHoliday(UUID b2bUnitId, UUID calendarId, UUID holidayId);
}
