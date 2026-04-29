package com.juviai.user.organisation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.User;
import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.domain.B2BGroup;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.domain.B2BUnitStatus;
import com.juviai.user.organisation.domain.B2BUnitType;
import com.juviai.user.organisation.domain.City;
import com.juviai.user.organisation.domain.State;
import com.juviai.user.organisation.domain.HolidayCalendar;
import com.juviai.user.organisation.domain.Holiday;
import com.juviai.user.organisation.repo.AddressRepository;
import com.juviai.user.organisation.repo.B2BUnitRepository;
import com.juviai.user.organisation.repo.CityRepository;
import com.juviai.user.organisation.repo.StateRepository;
import com.juviai.user.organisation.repo.HolidayCalendarRepository;
import com.juviai.user.organisation.repo.HolidayRepository;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.organisation.web.dto.CreateHolidayCalendarRequest;
import com.juviai.user.organisation.web.dto.CreateHolidayRequest;
import com.juviai.user.organisation.web.dto.UpdateCompanyCodeRequest;
import com.juviai.user.organisation.web.dto.OnboardRequest;
import com.juviai.user.organisation.web.mapper.OnboardingMapper;
import com.juviai.user.repo.UserRepository;
import com.juviai.user.service.UserService;

@Service
public class B2BUnitService {

    private static final Logger log = LoggerFactory.getLogger(B2BUnitService.class);

    private static final Set<String> PERSONAL_EMAIL_DOMAINS = Set.of(
            "gmail.com",
            "yahoo.com",
            "yahoo.co.in",
            "outlook.com",
            "hotmail.com",
            "live.com",
            "msn.com",
            "aol.com",
            "icloud.com",
            "me.com",
            "proton.me",
            "protonmail.com",
            "gmx.com",
            "gmx.net",
            "mail.com"
    );

    private final B2BUnitRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final B2BGroupService groupService;
    private final B2BUnitCategoryService b2bUnitCategoryService;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final AddressRepository addressRepository;
    private final HolidayCalendarRepository holidayCalendarRepository;
    private final HolidayRepository holidayRepository;

    public B2BUnitService(B2BUnitRepository repository,
                          UserRepository userRepository,
                          UserService userService,
                          B2BGroupService groupService,
                          B2BUnitCategoryService b2bUnitCategoryService,
                          StateRepository stateRepository,
                          CityRepository cityRepository,
                          AddressRepository addressRepository,
                          HolidayCalendarRepository holidayCalendarRepository,
                          HolidayRepository holidayRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userService = userService;
		this.groupService = groupService;
        this.b2bUnitCategoryService = b2bUnitCategoryService;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.addressRepository = addressRepository;
        this.holidayCalendarRepository = holidayCalendarRepository;
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public Page<Address> getAddresses(@NonNull UUID id, @NonNull Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("B2BUnit not found");
        }
        return addressRepository.findByB2bUnit_IdAndTenantId(id, tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<HolidayCalendar> getHolidayCalendars(@NonNull UUID id, @NonNull Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("B2BUnit not found");
        }
        return holidayCalendarRepository.findByB2bUnit_IdAndTenantId(id, tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public HolidayCalendar getHolidayCalendar(@NonNull UUID b2bUnitId, @NonNull UUID calendarId) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (!repository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2BUnit not found");
        }
        return holidayCalendarRepository.findByIdAndB2bUnit_IdAndTenantId(calendarId, b2bUnitId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("HolidayCalendar not found"));
    }

    @Transactional
    public HolidayCalendar createHolidayCalendar(@NonNull UUID id, @NonNull CreateHolidayCalendarRequest request) {
        if (request.getCityCode() == null || request.getCityCode().isBlank()) {
            throw new IllegalArgumentException("cityCode is required");
        }
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        B2BUnit unit = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found"));

        City city = cityRepository.findByCode(request.getCityCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("City not found with code: " + request.getCityCode()));

        if (holidayCalendarRepository.existsByCity_IdAndTenantId(city.getId(), tenantId)) {
            throw new IllegalStateException("HolidayCalendar already exists for city: " + city.getCode());
        }

        HolidayCalendar calendar = new HolidayCalendar();
        calendar.setTenantId(tenantId);
        calendar.setB2bUnit(unit);
        calendar.setCity(city);
        String name = request.getName();
        calendar.setName((name == null || name.isBlank()) ? ("Holiday Calendar - " + city.getCode()) : name.trim());

        return holidayCalendarRepository.save(calendar);
    }

    @Transactional
    public Holiday createHoliday(@NonNull UUID b2bUnitId, @NonNull UUID calendarId, @NonNull CreateHolidayRequest request) {
        if (request.getDate() == null) {
            throw new IllegalArgumentException("date is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (!repository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2BUnit not found");
        }

        HolidayCalendar calendar = holidayCalendarRepository
                .findByIdAndB2bUnit_IdAndTenantId(calendarId, b2bUnitId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("HolidayCalendar not found"));

        if (holidayRepository.existsByHolidayCalendar_IdAndDateAndTenantId(calendarId, request.getDate(), tenantId)) {
            throw new IllegalStateException("Holiday already exists for date: " + request.getDate());
        }

        Holiday holiday = new Holiday();
        holiday.setTenantId(tenantId);
        holiday.setHolidayCalendar(calendar);
        holiday.setDate(request.getDate());
        holiday.setName(request.getName().trim());

        return holidayRepository.save(holiday);
    }

    @Transactional(readOnly = true)
    public Page<Holiday> getHolidays(@NonNull UUID b2bUnitId, @NonNull UUID calendarId, @NonNull Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (!repository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2BUnit not found");
        }

        boolean calendarExists = holidayCalendarRepository
                .findByIdAndB2bUnit_IdAndTenantId(calendarId, b2bUnitId, tenantId)
                .isPresent();
        if (!calendarExists) {
            throw new IllegalArgumentException("HolidayCalendar not found");
        }

        return holidayRepository.findByHolidayCalendar_IdAndTenantIdOrderByDateAsc(calendarId, tenantId, pageable);
    }

    @Transactional
    public void deleteHoliday(@NonNull UUID b2bUnitId, @NonNull UUID calendarId, @NonNull UUID holidayId) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        if (!repository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2BUnit not found");
        }

        boolean calendarExists = holidayCalendarRepository
                .findByIdAndB2bUnit_IdAndTenantId(calendarId, b2bUnitId, tenantId)
                .isPresent();
        if (!calendarExists) {
            throw new IllegalArgumentException("HolidayCalendar not found");
        }

        long deleted = holidayRepository.deleteByIdAndHolidayCalendar_IdAndTenantId(holidayId, calendarId, tenantId);
        if (deleted == 0) {
            throw new IllegalArgumentException("Holiday not found");
        }
    }

    @Transactional
    public B2BUnit addAddress(@NonNull UUID id, @NonNull AddressDTO request) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        Address address = OnboardingMapper.toEntity(request);
        if (address == null) {
            throw new IllegalArgumentException("address is required");
        }
        address.setTenantId(tenantId);

        String stateCode = request.getState();
        if (stateCode != null && !stateCode.isBlank()) {
            State s = stateRepository.findByCode(stateCode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("State not found with code: " + stateCode));
            address.setState(s);
        }

        String cityCode = request.getCity();
        if (cityCode != null && !cityCode.isBlank()) {
            City c = cityRepository.findByCode(cityCode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("City not found with code: " + cityCode));
            address.setCity(c);
        }

        B2BUnit unit = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found"));
        unit.addAddress(address);
        return repository.save(unit);
    }

    @Transactional
    public B2BUnit selfSignup(OnboardRequest request) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");

        // Idempotent: if a B2BUnit with this name already exists for the tenant,
        // skip creation and just (re-)assign the caller as BUSINESS_ADMIN.
        // This handles the case where the unit was created before but the
        // user-link step failed (e.g. due to the email-vs-UUID identity bug).
        B2BUnit unit;
        if (repository.existsByNameIgnoreCaseAndTenantId(request.getName(), tenantId)) {
            unit = repository.findByNameIgnoreCaseAndTenantId(request.getName(), tenantId)
                    .orElseThrow(() -> new IllegalStateException("B2BUnit not found after existence check"));
            log.info("B2BUnit already exists id={}, re-assigning admin", unit.getId());
        } else {
            unit = createB2BUnit(request, tenantId, true);
            log.info("B2BUnit self signup created id={}, name={}, tenantId={}", unit.getId(), unit.getName(), tenantId);
        }

        // Assign the current user as BUSINESS_ADMIN.
        // Prefer UUID-based lookup (GatewayAuthFilter always sets auth.getName() = userId UUID).
        UUID callerUserId = getCurrentUserId();
        try {
            if (callerUserId != null) {
                userService.assignBusinessAdminById(unit.getId(), callerUserId);
                log.info("Assigned BUSINESS_ADMIN for unitId={}, userId={}", unit.getId(), callerUserId);
            } else {
                String callerEmail = getCurrentUserEmail();
                userService.assignBusinessAdmin(unit.getId(), callerEmail);
                log.info("Assigned BUSINESS_ADMIN for unitId={}, email={}", unit.getId(), callerEmail);
            }
        } catch (Exception ex) {
            log.warn("Failed to assign business-admin for unitId={}, userId={}: {}", unit.getId(), callerUserId, ex.getMessage(), ex);
        }
        return unit;
    }

    public B2BUnit onBoardByAdminSignup(OnboardRequest request) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        B2BUnit unit=createB2BUnit(request,tenantId, true);
        log.info("B2BUnit self signup created id={}, name={}, tenantId= {}", unit.getId(), unit.getName(), tenantId);

        return unit;
    }

    @Transactional
    public B2BUnit adminImportCreate(OnboardRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        request.setAdminOnboardRequest(true);
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        B2BUnit unit = createB2BUnit(request, tenantId, false);
        log.info("B2BUnit admin import created id={}, name={}, tenantId= {}", unit.getId(), unit.getName(), tenantId);
        return unit;
    }

    @Transactional
    public B2BUnit approve(@NonNull UUID id, String approver) {
        B2BUnit unit = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found"));
        unit.setStatus(B2BUnitStatus.APPROVED);
        User approverUser = resolveUserByEmail(approver);
        if (approverUser == null) {
            approverUser = getCurrentUser();
        }
        unit.setApprovedBy(approverUser);
        unit.setApprovedAt(Instant.now());
        B2BUnit saved = repository.save(unit);
        log.info("B2BUnit approved id={}, name={}, approverUserId={}", saved.getId(), saved.getName(),
                saved.getApprovedBy() != null ? saved.getApprovedBy().getId() : null);
        return saved;
    }

    @Transactional(readOnly = true)
    public B2BUnit findById(@NonNull UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found"));
    }

    @Transactional
    public B2BUnit updateCompanyCode(@NonNull UUID id, @NonNull UpdateCompanyCodeRequest request) {
        if (request.getCompanyCode() == null || request.getCompanyCode().trim().isBlank()) {
            throw new IllegalArgumentException("companyCode is required");
        }
        String trimmedCompanyCode = request.getCompanyCode().trim();

        B2BUnit unit = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found"));
        unit.setCompanyCode(trimmedCompanyCode);

        if (request.getTanNumber() != null) {
            unit.setTanNumber(request.getTanNumber().trim());
        }
        if (request.getCinNumber() != null) {
            unit.setCinNumber(request.getCinNumber().trim());
        }
        if (request.getGstNumber() != null) {
            unit.setGstNumber(request.getGstNumber().trim());
        }
        if (request.getPanNumber() != null) {
            unit.setPanNumber(request.getPanNumber().trim());
        }
        if (request.getSalaryDate() != null) {
            unit.setSalaryDate(request.getSalaryDate());
        }

        if (request.getIsStartup() != null) {
            unit.setIsStartup(request.getIsStartup());
        }
        if (request.getIsBootstrapped() != null) {
            unit.setIsBootstrapped(request.getIsBootstrapped());
        }

        return repository.save(unit);
    }

    public Page<B2BUnit> listPending(Pageable pageable) {
        return repository.findByStatus(B2BUnitStatus.PENDING_APPROVAL, pageable);
    }

    @Transactional(readOnly = true)
    public Page<B2BUnit> searchByName(String query, Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        String q = query != null ? query.trim() : null;
        if (q == null || q.length() < 3) {
            return Page.empty(pageable);
        }
        return repository.findByTenantIdAndTypeAndNameContainingIgnoreCase(tenantId, B2BUnitType.COLLEGE, q, pageable);
    }


    /**
     * Add an existing user as a business admin of the specified B2BUnit.
     *
     * <p>Looks up the user by {@code userId} (primary) or falls back to {@code email}.
     * Delegates to {@link UserService#assignBusinessAdmin(UUID, String)} which:
     * <ul>
     *   <li>Creates/finds the {@code ROLE_BUSINESS_ADMIN} role scoped to this B2BUnit.</li>
     *   <li>Assigns the role + {@code ROLE_USER} to the user.</li>
     *   <li>Links the user to the B2BUnit via {@code user.b2bUnit}.</li>
     * </ul>
     *
     * @param b2bUnitId  UUID of the B2BUnit the user will administer
     * @param userId     UUID of the user to promote (optional when email is provided)
     * @param email      email of the user to promote (used when userId is null)
     */
    @Transactional
    public void addAdmin(@NonNull UUID b2bUnitId, UUID userId, String email) {
        if (!repository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2BUnit not found: " + b2bUnitId);
        }

        String resolvedEmail;
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            resolvedEmail = user.getEmail();
        } else if (email != null && !email.isBlank()) {
            resolvedEmail = email.trim();
            if (userRepository.findByEmailIgnoreCase(resolvedEmail).isEmpty()) {
                throw new IllegalArgumentException("User not found with email: " + resolvedEmail);
            }
        } else {
            throw new IllegalArgumentException("Either userId or email must be provided");
        }

        userService.assignBusinessAdmin(b2bUnitId, resolvedEmail);
        log.info("Added business admin email={} to b2bUnitId={}", resolvedEmail, b2bUnitId);
    }

    public Page<B2BUnit> adminList(String q, Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        String query = (q == null) ? null : q.trim();
        if (query == null || query.isBlank()) {
            return repository.findByTenantId(tenantId, pageable);
        }
        return repository.searchAdmin(tenantId, query, pageable);
    }

    /**
     * Resolves the currently authenticated user.
     *
     * <p>Strategy (in order of preference):
     * <ol>
     *   <li>UUID from the {@code userId} attribute on the {@link OAuth2AuthenticatedPrincipal}
     *       — this is what {@link com.juviai.common.security.GatewayAuthFilter} always sets
     *       from the {@code X-User-Id} header, and is the most reliable identifier.</li>
     *   <li>Email lookup via {@link #getCurrentUserEmail()} as a fallback for legacy/JWT paths.</li>
     * </ol>
     */
    private User getCurrentUser() {
        // Primary path: userId UUID attribute (set by GatewayAuthFilter from X-User-Id)
        UUID userId = getCurrentUserId();
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        // Fallback: email-based lookup (JWT / opaque introspection paths)
        String email = getCurrentUserEmail();
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmailIgnoreCase(email).orElse(null);
        }
        return null;
    }

    /**
     * Extracts the authenticated user's UUID from the security context.
     * Returns {@code null} when the context contains no valid UUID identity.
     */
    private UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;

            // GatewayAuthFilter sets OAuth2AuthenticatedPrincipal with "userId" attribute
            Object principal = auth.getPrincipal();
            if (principal instanceof OAuth2AuthenticatedPrincipal oauth2) {
                String uid = oauth2.getAttribute("userId");
                if (uid != null && !uid.isBlank()) {
                    return java.util.UUID.fromString(uid);
                }
            }
            // auth.getName() is also the userId UUID when set by GatewayAuthFilter
            String name = auth.getName();
            if (name != null && !name.isBlank()) {
                try { return java.util.UUID.fromString(name); } catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    private String getCurrentUserEmail() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return null;
            }

            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String email = jwtAuth.getToken().getClaimAsString("email");
                if (email != null && !email.isBlank()) {
                    return email;
                }
            }

            if (auth instanceof BearerTokenAuthentication bearer) {
                Object principal = bearer.getPrincipal();
                if (principal instanceof OAuth2AuthenticatedPrincipal oauth2) {
                    String email = oauth2.getAttribute("email");
                    if (email != null && !email.isBlank()) {
                        return email;
                    }
                    String username = oauth2.getAttribute("username");
                    if (username != null && !username.isBlank()) {
                        return username;
                    }
                    String sub = oauth2.getAttribute("sub");
                    if (sub != null && !sub.isBlank() && sub.contains("@")) {
                        return sub;
                    }
                }
            }

            Object principal = auth.getPrincipal();
            if (principal instanceof OAuth2AuthenticatedPrincipal oauth2) {
                String email = oauth2.getAttribute("email");
                if (email != null && !email.isBlank()) {
                    return email;
                }
                String username = oauth2.getAttribute("username");
                if (username != null && !username.isBlank()) {
                    return username;
                }
            }

            String name = auth.getName();
            return (name != null && !name.isBlank()) ? name : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private User resolveUserByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        try {
            return userRepository.findByEmailIgnoreCase(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void requireBusinessEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Business email is required");
        }
        String trimmed = email.trim();
        int at = trimmed.lastIndexOf('@');
        if (at < 0 || at == trimmed.length() - 1) {
            throw new IllegalArgumentException("Invalid email address");
        }
        String domain = trimmed.substring(at + 1).toLowerCase();
        if (PERSONAL_EMAIL_DOMAINS.contains(domain)) {
            throw new IllegalArgumentException("Please use your business email address");
        }
    }
    
    private B2BUnit createB2BUnit(OnboardRequest request,String tenantId, boolean selfOnboard) {
        String loggedInEmail = getCurrentUserEmail();

        if(!request.isAdminOnboardRequest()){
            if (selfOnboard) {
                requireBusinessEmail(loggedInEmail);
            } else if (loggedInEmail != null && !loggedInEmail.isBlank()) {
                requireBusinessEmail(loggedInEmail);
            }
            if (request.getContactEmail() != null && !request.getContactEmail().isBlank()) {
                requireBusinessEmail(request.getContactEmail());
            }
        }

        if (repository.existsByNameIgnoreCaseAndTenantId(request.getName(), tenantId)) {
            throw new IllegalStateException("B2BUnit with name already exists for tenant");
        }
        B2BUnit unit = new B2BUnit();
        unit.setName(request.getName());
        unit.setType(request.getType());
        unit.setContactEmail(request.getContactEmail());
        unit.setContactPhone(request.getContactPhone());
        unit.setWebsite(request.getWebsite());
        unit.setLogo(request.getLogo());
        unit.setBrandTagLine(request.getBrandTagLine());
        unit.setStartupDescription(request.getStartupDescription());
        unit.setTargetAudience(request.getTargetAudience());
        unit.setRevenueModel(request.getRevenueModel());
        unit.setFindCoFounder(request.isFindCoFounder());
        unit.setBuildSolo(request.isBuildSolo());
        unit.setInviteCoFounder(request.isInviteCoFounder());
        unit.setStudentStartup(request.isStudentStartup());
        if (request.getAdditionalAttributes() != null) {
            unit.setAdditionalAttributes(new java.util.HashMap<>(request.getAdditionalAttributes()));
        }
        if (Objects.nonNull(request.getCategory())) {
            unit.setCategory(b2bUnitCategoryService.getById(request.getCategory()));
        }

        Address address = OnboardingMapper.toEntity(request.getAddress());
        if (Objects.nonNull(address)) {
            address.setTenantId(tenantId);

            if (request.getAddress() != null) {
                String stateCode = request.getAddress().getState();
                if (stateCode != null && !stateCode.isBlank()) {
                    State s = stateRepository.findByCode(stateCode.trim())
                            .orElseThrow(() -> new IllegalArgumentException("State not found with code: " + stateCode));
                    address.setState(s);
                }

                String cityCode = request.getAddress().getCity();
                if (cityCode != null && !cityCode.isBlank()) {
                    City c = cityRepository.findByCode(cityCode.trim())
                            .orElseThrow(() -> new IllegalArgumentException("City not found with code: " + cityCode));
                    address.setCity(c);
                }
            }

            unit.addAddress(address);
        }

        unit.setTenantId(tenantId);
        unit.setOnboardedBy(getCurrentUser());
        if(selfOnboard) {
        	unit.setStatus(B2BUnitStatus.PENDING_APPROVAL);
		} else {
			unit.setStatus(B2BUnitStatus.APPROVED);
			unit.setApprovedBy(getCurrentUser());
			unit.setApprovedAt(Instant.now());
		}
        return repository.save(unit);
    }
}
