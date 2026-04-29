package com.juviai.user.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.Designation;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.EmploymentType;
import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.CompensationType;
import com.juviai.user.domain.EmployeeCodeSequence;
import com.juviai.user.converter.EmployeeDetailsConverter;
import com.juviai.user.dto.CreateEmployeeRequestDto;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.dto.UpdateBankAccountRequestDto;
import com.juviai.user.dto.UpdateEmployeeRequestDto;
import com.juviai.user.domain.BankAccount;
import com.juviai.user.domain.User;
import com.juviai.user.crypto.KeyVaultEnvelopeEncryptionService;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.domain.Department;
import com.juviai.user.organisation.repo.B2BUnitRepository;
import com.juviai.user.organisation.repo.DepartmentRepository;
import com.juviai.user.repo.BankAccountRepository;
import com.juviai.user.repo.EmployeeCodeSequenceRepository;
import com.juviai.user.repo.EmployeeRepository;
import com.juviai.user.repo.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeCodeSequenceRepository employeeCodeSequenceRepository;
    private final B2BUnitRepository b2bUnitRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final KeyVaultEnvelopeEncryptionService keyVaultEnvelopeEncryptionService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeBandService service;
    private final DesignationService designationService;
    private final DepartmentRepository departmentRepository;
    private final EmployeeDetailsConverter employeeDetailsConverter;
    private final CompensationService compensationService;
    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeCodeSequenceRepository employeeCodeSequenceRepository,
                           B2BUnitRepository b2bUnitRepository,
                           UserRepository userRepository,
                           BankAccountRepository bankAccountRepository,
                           KeyVaultEnvelopeEncryptionService keyVaultEnvelopeEncryptionService,
                           PasswordEncoder passwordEncoder,
                           MailService mailService,
                           EmployeeBandService service,
                           DesignationService designationService,
                           DepartmentRepository departmentRepository,
                           EmployeeDetailsConverter employeeDetailsConverter,
                           CompensationService compensationService) {
        this.employeeRepository = employeeRepository;
        this.employeeCodeSequenceRepository = employeeCodeSequenceRepository;
        this.b2bUnitRepository = b2bUnitRepository;
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.keyVaultEnvelopeEncryptionService = keyVaultEnvelopeEncryptionService;
        this.passwordEncoder = passwordEncoder;
        this.service = service;

        this.designationService = designationService;
        this.departmentRepository = departmentRepository;
        this.employeeDetailsConverter = employeeDetailsConverter;
        this.compensationService = compensationService;
    }

    private String generateEmployeeCode(UUID b2bUnitId) {
        if (b2bUnitId == null) {
            throw new IllegalArgumentException("b2bUnitId is required to generate employeeCode");
        }

        B2BUnit unit = b2bUnitRepository.findById(b2bUnitId)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found"));

        String companyCode = unit.getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalArgumentException("companyCode is required on B2BUnit to generate employeeCode");
        }
        String prefix = companyCode.trim().toUpperCase();

        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                EmployeeCodeSequence seq = employeeCodeSequenceRepository.findByB2bUnitIdForUpdate(b2bUnitId)
                        .orElseGet(() -> {
                            EmployeeCodeSequence s = new EmployeeCodeSequence();
                            s.setB2bUnitId(b2bUnitId);
                            s.setCompanyCode(prefix);
                            s.setNextValue(1L);
                            return s;
                        });

                long current = seq.getNextValue();
                seq.setNextValue(current + 1L);
                employeeCodeSequenceRepository.save(seq);

                return prefix + String.format("%05d", current);
            } catch (DataIntegrityViolationException e) {
                if (attempt == 0) {
                    continue;
                }
                throw e;
            }
        }

        throw new IllegalStateException("Unable to generate employeeCode");
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(UUID b2bUnitId, String q, EmploymentType type, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<Employee> page = employeeRepository.search(b2bUnitId, query, type, pageable);
        try {
            List<String> rawEmails = employeeRepository.debugEmployeeEmailsByB2bUnitId(String.valueOf(b2bUnitId));
            List<String> b2bRow = employeeRepository.debugB2bUnitRow(String.valueOf(b2bUnitId));
            List<Employee> byB2b = employeeRepository.findByB2bUnitId(b2bUnitId);
            String details = page.getContent().stream()
                    .map(e -> String.valueOf(e.getId()) + "/" + e.getEmail())
                    .collect(java.util.stream.Collectors.joining(", "));
            log.info("Employee search b2bUnitId={} q={} type={} page={} size={} -> total={} returned={} [{}]",
                    b2bUnitId, query, type,
                    pageable != null ? pageable.getPageNumber() : null,
                    pageable != null ? pageable.getPageSize() : null,
                    page.getTotalElements(), page.getNumberOfElements(), details);
            log.info("Employee search raw users table rows for b2bUnitId={} tenantContext={} -> {}",
                    b2bUnitId, TenantContext.getTenantId(), rawEmails);
            log.info("Employee search b2b_unit row for b2bUnitId={} -> {}", b2bUnitId, b2bRow);
            log.info("Employee findByB2bUnitId b2bUnitId={} -> count={} [{}]",
                    b2bUnitId,
                    byB2b != null ? byB2b.size() : null,
                    byB2b != null
                            ? byB2b.stream().map(e -> String.valueOf(e.getId()) + "/" + e.getEmail())
                                    .collect(java.util.stream.Collectors.joining(", "))
                            : null);
        } catch (Exception ignored) {
            // do nothing
        }
        return page;
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getById(@NonNull UUID id) {
        return employeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDetailsDto> getDetailsById(@NonNull UUID id) {
        return employeeRepository.findById(id).map(employeeDetailsConverter::convert);
    }

    @Transactional(readOnly = true)
    public List<Employee> listByB2bUnit(UUID b2bUnitId) {
        return employeeRepository.findByB2bUnitId(b2bUnitId);
    }

    @SuppressWarnings("null")
    @Transactional
    public Employee create(@NonNull CreateEmployeeRequestDto req) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        // Admin validations: unique email/mobile, name present
        userRepository.findByEmailIgnoreCase(req.getEmail()).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
        if (req.getMobile() != null && !req.getMobile().isBlank()) {
            userRepository.findByMobile(req.getMobile()).ifPresent(u -> { throw new IllegalArgumentException("Mobile already in use"); });
        }
        Employee e = new Employee();
        e.setFirstName(Objects.requireNonNull(req.getFirstName(), "firstName is required"));
        e.setLastName(Objects.requireNonNull(req.getLastName(), "lastName is required"));
        e.setUsername(req.getEmail().toLowerCase());
        e.setEmail(req.getEmail().toLowerCase());
        e.setMobile(req.getMobile());
        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            e.setDepartment(dept);
        }
        e.setStoreId(req.getStoreId());
        e.setEmploymentType(req.getEmploymentType());
        if (req.getBandId() != null) {
           e.setBand(service.getBand(req.getBandId()));
        }
        e.setHireDate(req.getHireDate());
        if (req.getAnnualSalary() != null) {
            e.setAnnualSalary(req.getAnnualSalary());
        }
        if (req.getReportingManagerId() != null) {
            User rm = userRepository.findById(req.getReportingManagerId()).orElseThrow(() -> new IllegalArgumentException("Reporting manager not found"));
            e.setReportingManager(rm);
        }
        e.setEmployeeCode(generateEmployeeCode(req.getB2bUnitId()));
        String defaultPassword = "Password@123";
        e.setPasswordHash(passwordEncoder.encode(defaultPassword));
        e.setActive(true);
        e.setTenantId(tenantId);
        if (req.getB2bUnitId() != null) {
            B2BUnit bu = new B2BUnit();
            bu.setId(req.getB2bUnitId());
            e.setB2bUnit(bu);
        }
        e.setPasswordNeedsReset(true);
        e.setPasswordSetupToken(UUID.randomUUID().toString());
        e.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        Designation design = designationService.findById(req.getDesignation());
        e.setDesignation(design);
        Employee saved = employeeRepository.save(e);

        if (req.getAnnualSalary() != null && saved.getHireDate() != null) {
            Compensation c = new Compensation();
            c.setEmployee(saved);
            c.setType(CompensationType.SALARY);
            c.setAmount(req.getAnnualSalary());
            c.setEffectiveStartDate(saved.getHireDate());
            c.setEffectiveEndDate(null);
            c.setActive(true);
            compensationService.create(c);
        }

        return saved;
    }

    @SuppressWarnings("null")
    @Transactional
    public Employee update(@NonNull UUID id, @NonNull UpdateEmployeeRequestDto req) {
        Employee e = employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        BigDecimal oldSalary = e.getAnnualSalary();
        if (req.getFirstName() != null && !req.getFirstName().isBlank()) e.setFirstName(req.getFirstName().trim());
        if (req.getLastName() != null && !req.getLastName().isBlank()) e.setLastName(req.getLastName().trim());
        if (req.getDateOfBirth() != null) e.setDateOfBirth(req.getDateOfBirth());
        if (req.getGender() != null) e.setGender(req.getGender());
        if (req.getEmail() != null && !req.getEmail().isBlank()) e.setEmail(req.getEmail().trim());
        if (req.getStoreId() != null) e.setStoreId(req.getStoreId());
        if (req.getAnnualSalary() != null) e.setAnnualSalary(req.getAnnualSalary());
        if (req.getPfNumber() != null && !req.getPfNumber().isBlank()) e.setPfNumber(req.getPfNumber().trim());
        if (req.getUanNumber() != null && !req.getUanNumber().isBlank()) e.setUanNumber(req.getUanNumber().trim());
        if (req.getPanNumber() != null && !req.getPanNumber().isBlank()) e.setPanNumber(req.getPanNumber().trim());
        if (req.isPfEnabled()) e.setPfEnabled(true);
        if (req.getReportingManagerId() != null) {
            User rm = userRepository.findById(req.getReportingManagerId()).orElseThrow(() -> new IllegalArgumentException("Reporting manager not found"));
            e.setReportingManager(rm);
        }
        if (req.getMobile() != null && !req.getMobile().isBlank()) {
            userRepository.findByMobile(req.getMobile())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> { throw new IllegalArgumentException("Mobile already in use"); });
            e.setMobile(req.getMobile());
        }
        if (req.getDesignation() != null) {
            e.setDesignation((designationService.findById(req.getDesignation())));
        }
        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            e.setDepartment(dept);
        }
        if (req.getEmploymentType() != null) e.setEmploymentType(req.getEmploymentType());
        if (req.getBandId() != null) {
            e.setBand(service.getBand(req.getBandId()));
        }
        if (req.getHireDate() != null) e.setHireDate(req.getHireDate());
        if (req.getReportingManagerId() != null) {
            User rm = userRepository.findById(req.getReportingManagerId()).orElseThrow(() -> new IllegalArgumentException("Reporting manager not found"));
            e.setReportingManager(rm);
        }

        Employee saved = employeeRepository.save(e);

        if (req.getAnnualSalary() != null
                && (oldSalary == null || oldSalary.compareTo(req.getAnnualSalary()) != 0)
                && saved.getHireDate() != null) {

            if (!compensationService.hasAny(saved.getId(), CompensationType.SALARY)) {
                Compensation c = new Compensation();
                c.setEmployee(saved);
                c.setType(CompensationType.SALARY);
                c.setAmount(req.getAnnualSalary());
                c.setEffectiveStartDate(saved.getHireDate());
                c.setEffectiveEndDate(null);
                c.setActive(true);
                compensationService.create(c);
            } else {
                Compensation next = new Compensation();
                next.setEmployee(saved);
                next.setType(CompensationType.SALARY);
                next.setAmount(req.getAnnualSalary());
                next.setEffectiveStartDate(LocalDate.now());
                next.setEffectiveEndDate(null);
                next.setActive(true);
                compensationService.reviseActiveCompensation(saved.getId(), CompensationType.SALARY, next);
            }
        }

        return saved;
    }

    @Transactional
    public void deleteUser(@NonNull UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        // Step 1: Remove dependent mapping first
        employeeRepository.deleteById(userId);
        // Step 2: Delete User
        userRepository.deleteById(userId);
    }

    @Transactional
    public void updateBankAccount(@NonNull UUID userId, @NonNull UpdateBankAccountRequestDto req) {
        Employee e = employeeRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (req.getAccountNumber() == null || req.getAccountNumber().isBlank()) {
            throw new IllegalArgumentException("accountNumber is required");
        }
        if (req.getIfscCode() == null || req.getIfscCode().isBlank()) {
            throw new IllegalArgumentException("ifscCode is required");
        }

        BankAccount ba = bankAccountRepository.findByUserId(e.getId()).orElseGet(BankAccount::new);
        ba.setUser(e);
        ba.setAccountNumber(keyVaultEnvelopeEncryptionService.encryptToPayload(req.getAccountNumber().trim()));
        ba.setIfscCode(keyVaultEnvelopeEncryptionService.encryptToPayload(req.getIfscCode().trim()));
        ba=bankAccountRepository.save(ba);
        log.info("Bank account updated successfully for employee: {}", e.getId());
    }

    @Transactional
    public Employee setDismissed(@NonNull UUID employeeId, @NonNull Boolean dismissed) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        boolean shouldDismiss = dismissed;
        e.setDismissed(shouldDismiss);
        e.setActive(!shouldDismiss);
        return employeeRepository.save(e);
    }
}
