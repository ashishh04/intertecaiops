package com.juviai.user.organisation.web;

import com.juviai.user.organisation.facade.B2BUnitFacade;
import com.juviai.user.organisation.importer.ExcelB2BUnitImportParser;
import com.juviai.user.organisation.service.B2BUnitService;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.organisation.web.dto.B2BUnitDTO;
import com.juviai.user.organisation.web.dto.B2BUnitExcelImportResponse;
import com.juviai.user.organisation.web.dto.CreateHolidayCalendarRequest;
import com.juviai.user.organisation.web.dto.CreateHolidayRequest;
import com.juviai.user.organisation.web.dto.HolidayCalendarDTO;
import com.juviai.user.organisation.web.dto.HolidayDTO;
import com.juviai.user.organisation.web.dto.OnboardRequest;
import com.juviai.user.organisation.web.dto.UpdateCompanyCodeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.juviai.user.organisation.web.dto.AddB2BAdminRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/b2b")
@Validated
public class B2BUnitController {

    private final B2BUnitFacade b2bUnitFacade;
    private final B2BUnitService b2bUnitService;
    private final ExcelB2BUnitImportParser excelB2BUnitImportParser;

    public B2BUnitController(B2BUnitFacade b2bUnitFacade,
                             B2BUnitService b2bUnitService,
                             ExcelB2BUnitImportParser excelB2BUnitImportParser) {
        this.b2bUnitFacade = b2bUnitFacade;
        this.b2bUnitService = b2bUnitService;
        this.excelB2BUnitImportParser = excelB2BUnitImportParser;
    }

    @PostMapping("/onboard/self")
    @PreAuthorize("isAuthenticated()")
    public B2BUnitDTO selfOnboard(@RequestBody @Validated OnboardRequest request) {
        return b2bUnitFacade.selfOnboard(request);
    }

    @PostMapping("/onboard/byadmin")
    @PreAuthorize("isAuthenticated()")
    public B2BUnitDTO b2bUnitOnboard(@RequestBody @Validated OnboardRequest request) {
        return b2bUnitFacade.selfOnboard(request);
    }

    /**
     * Add an existing user as a business admin (ROLE_BUSINESS_ADMIN) of the B2BUnit.
     *
     * <pre>
     * POST /api/b2b/{id}/admin
     * Body: { "userId": "uuid" }   OR   { "email": "user@company.com" }
     * </pre>
     *
     * <p>Only platform ADMINs may promote users to business admin.
     * After the call the user will have {@code ROLE_BUSINESS_ADMIN} scoped to this
     * B2BUnit and can log in to the business dashboard to manage stores.
     */
    @PostMapping("/{id}/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Map<String, String> addAdmin(
            @PathVariable("id") @NonNull UUID id,
            @RequestBody AddB2BAdminRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required");
        }
        b2bUnitFacade.addAdmin(id, request.getUserId(), request.getEmail());
        return Map.of("status", "admin_assigned",
                      "b2bUnitId", id.toString());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public B2BUnitDTO approve(@PathVariable("id") @NonNull UUID id,
                              @RequestBody Map<String, String> body) {
        String approver = body != null ? body.getOrDefault("approver", "skillrat-admin") : "skillrat-admin";
        return b2bUnitFacade.approve(id, approver);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<B2BUnitDTO> pending(Pageable pageable) {
        return b2bUnitFacade.pending(pageable);
    }

    @GetMapping("/search")
    public Page<B2BUnitDTO> search(@RequestParam("q") String q, Pageable pageable) {
        return b2bUnitFacade.search(q, pageable);
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<B2BUnitDTO> adminList(@RequestParam(value = "q", required = false) String q, Pageable pageable) {
        return b2bUnitFacade.adminList(q, pageable);
    }

    @PostMapping(value = "/admin/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<B2BUnitExcelImportResponse> importExcel(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        List<ExcelB2BUnitImportParser.ImportRowError> errors = new ArrayList<>();
        List<ExcelB2BUnitImportParser.B2BUnitImportRow> rows;
        try {
            rows = excelB2BUnitImportParser.parse(file.getInputStream(), errors);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage());
        }

        int created = 0;
        for (ExcelB2BUnitImportParser.B2BUnitImportRow r : rows) {
            try {
                b2bUnitService.adminImportCreate(r.toOnboardRequest());
                created++;
            } catch (Exception e) {
                errors.add(new ExcelB2BUnitImportParser.ImportRowError(r.getRowNumber(), r.getName(), e.getMessage()));
            }
        }

        B2BUnitExcelImportResponse resp = new B2BUnitExcelImportResponse();
        resp.setTotalRows(rows.size());
        resp.setCreatedCount(created);
        resp.setFailedCount(errors.size());
        resp.setErrors(errors);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public B2BUnitDTO getById(@PathVariable("id") @NonNull UUID id) {
        return b2bUnitFacade.getById(id);
    }

    @PutMapping("/{id}/company-code")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public B2BUnitDTO updateCompanyCode(@PathVariable("id") @NonNull UUID id,
                                        @RequestBody UpdateCompanyCodeRequest request) {
        return b2bUnitFacade.updateCompanyCode(id, request);
    }

    @GetMapping("/{id}/addresses")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public Page<AddressDTO> getAddresses(@PathVariable("id") @NonNull UUID id, Pageable pageable) {
        return b2bUnitFacade.getAddresses(id, pageable);
    }

    @PostMapping("/{id}/addresses")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public B2BUnitDTO addAddress(@PathVariable("id") @NonNull UUID id,
                                 @RequestBody AddressDTO request) {
        return b2bUnitFacade.addAddress(id, request);
    }

    @GetMapping("/{id}/holiday-calendars")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public Page<HolidayCalendarDTO> getHolidayCalendars(@PathVariable("id") @NonNull UUID id, Pageable pageable) {
        return b2bUnitFacade.getHolidayCalendars(id, pageable);
    }

    @PostMapping("/{id}/holiday-calendars")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public HolidayCalendarDTO createHolidayCalendar(@PathVariable("id") @NonNull UUID id,
                                                    @RequestBody CreateHolidayCalendarRequest request) {
        return b2bUnitFacade.createHolidayCalendar(id, request);
    }

    @GetMapping("/{id}/holiday-calendars/{calendarId}")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public HolidayCalendarDTO getHolidayCalendar(@PathVariable("id") @NonNull UUID id,
                                                 @PathVariable("calendarId") @NonNull UUID calendarId) {
        return b2bUnitFacade.getHolidayCalendar(id, calendarId);
    }

    @PostMapping("/{id}/holiday-calendars/{calendarId}/holiday")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public HolidayDTO createHoliday(@PathVariable("id") @NonNull UUID id,
                                    @PathVariable("calendarId") @NonNull UUID calendarId,
                                    @RequestBody CreateHolidayRequest request) {
        return b2bUnitFacade.createHoliday(id, calendarId, request);
    }

    @GetMapping("/{id}/holiday-calendars/{calendarId}/holidays")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public Page<HolidayDTO> getHolidays(@PathVariable("id") @NonNull UUID id,
                                        @PathVariable("calendarId") @NonNull UUID calendarId,
                                        Pageable pageable) {
        return b2bUnitFacade.getHolidays(id, calendarId, pageable);
    }

    @DeleteMapping("/{id}/holiday-calendars/{calendarId}/holidays/{holidayId}")
    @PreAuthorize("hasRole('ADMIN') or @b2bSecurity.hasBusinessOrHrAdmin(#id)")
    public void deleteHoliday(@PathVariable("id") @NonNull UUID id,
                              @PathVariable("calendarId") @NonNull UUID calendarId,
                              @PathVariable("holidayId") @NonNull UUID holidayId) {
        b2bUnitFacade.deleteHoliday(id, calendarId, holidayId);
    }
}
