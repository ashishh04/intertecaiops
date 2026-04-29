package com.juviai.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.web.dto.SignupRequest;
import com.juviai.user.domain.Experience;
import com.juviai.user.domain.UserStatus;
import com.juviai.user.organisation.domain.Department;
import com.juviai.user.organisation.repo.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.juviai.user.web.dto.InviteEmployeeRequest;
import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import com.juviai.user.repo.ExperienceRepository;
import com.juviai.user.repo.RoleRepository;
import com.juviai.user.repo.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExperienceRepository experienceRepository;
    private final DepartmentRepository departmentRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       ExperienceRepository experienceRepository,
                       DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.experienceRepository = experienceRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Experience> myExperienceDetails(String email) {
        UUID userId = userRepository.findByEmailIgnoreCase(email)
                .map(User::getId)
                .orElse(null);
        if (userId == null) {
            return List.of();
        }

        List<Experience> experiences = experienceRepository.findByUser_Id(userId);
        return experiences != null ? experiences : List.of();
    }

    /**
     * Preferred overload — looks up experience details directly by userId UUID.
     * Use this from any context where the identity comes from the gateway
     * (auth.getName() returns a UUID, not an email).
     */
    @Transactional(readOnly = true)
    public List<Experience> myExperienceDetailsById(UUID userId) {
        if (userId == null) return List.of();
        List<Experience> experiences = experienceRepository.findByUser_Id(userId);
        return experiences != null ? experiences : List.of();
    }

    private String currentTenant() {
        return Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    }

    private void setB2BUnitIfPresent(User u, UUID b2bUnitId) {
        if (b2bUnitId != null) {
            com.juviai.user.organisation.domain.B2BUnit bu = new com.juviai.user.organisation.domain.B2BUnit();
            bu.setId(b2bUnitId);
            u.setB2bUnit(bu);
        }
    }

    private String normalizeRoleName(String name) {
        if (name == null) return null;
        String n = name.trim();
        if (n.isBlank()) return n;

        // Collapse any tenant-scoped business admin variants (e.g. ROLE_BUSINESS_ADMIN_<id>)
        // into the canonical role name used across services.
        if (n.startsWith("ROLE_BUSINESS_ADMIN")) {
            return Role.ROLE_BUSINESS_ADMIN;
        }

        // Backward compatibility for older code paths that used BUSINESS_ADMIN without ROLE_ prefix.
        if ("BUSINESS_ADMIN".equalsIgnoreCase(n) || "ROLE_BUSINESS_OWNER".equalsIgnoreCase(n)) {
            return Role.ROLE_BUSINESS_ADMIN;
        }

        return n;
    }

    private Role ensureRole(String name, String description, UUID b2bUnitId) {
        String normalizedName = normalizeRoleName(name);

        // First try to find the role by name and b2bUnitId
        Optional<Role> existingRole = roleRepository.findByNameAndB2bUnitId(normalizedName, b2bUnitId);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }
        
        // If not found, check if a role with the same name exists (regardless of b2bUnitId)
        Optional<Role> roleWithSameName = roleRepository.findByName(normalizedName);
        if (roleWithSameName.isPresent()) {
            return roleWithSameName.get();
        }
        
        // No role with this name exists, create a new one
        Role role = new Role(normalizedName, description, b2bUnitId);
        return roleRepository.save(role);
    }

    private Integer parseYear(String year) {
        if (year == null) return null;
        String y = year.trim();
        if (y.isBlank()) return null;
        try {
            return Integer.valueOf(y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
    }

    private void upsertStudentExperience(User user, SignupRequest req, String tenantId) {
        List<Experience> existing = experienceRepository.findByUser_Id(user.getId());
        Experience exp = (existing != null && !existing.isEmpty()) ? existing.getFirst() : new Experience();
        exp.setUser(user);
        exp.setTenantId(tenantId);

        if (req.collegeUUID != null) {
            B2BUnit bu = new B2BUnit();
            bu.setId(req.collegeUUID);
            exp.setB2bUnit(bu);
        }

        if (req.branchCode != null && !req.branchCode.trim().isBlank()) {
            Department dept = departmentRepository.findByCode(req.branchCode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found for code: " + req.branchCode));
            exp.setDepartment(dept);
        }

        exp.setStartYear(parseYear(req.startYear));
        exp.setEndYear(parseYear(req.endYear));
        experienceRepository.save(exp);
    }

    /**
     * Sign up a new user. CLAUDE.md rule 1 — mobile is the primary identifier;
     * email is optional and may be provided later. When email is absent the
     * user's {@code username} falls back to their mobile number.
     */
    @Transactional
    public User signup(SignupRequest req) {
        String tenantId = currentTenant();

        boolean hasEmail = req.email != null && !req.email.isBlank();
        String normalizedEmail = hasEmail ? req.email.toLowerCase().trim() : null;
        String trimmedMobile = req.mobile != null ? req.mobile.trim() : null;
        if (trimmedMobile == null || trimmedMobile.isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }

        User existing = hasEmail
                ? userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null)
                : null;
        if (existing == null) {
            User byMobile = userRepository.findByMobile(trimmedMobile).orElse(null);
            if (byMobile != null) {
                if (byMobile.getStatus() != null && byMobile.getStatus() != UserStatus.PENDING_VERIFICATION) {
                    throw new IllegalArgumentException("Mobile number already in use");
                }
                existing = byMobile;
            }
        }
        if (existing != null) {
            if (existing.getStatus() != null && existing.getStatus() != UserStatus.PENDING_VERIFICATION) {
                throw new IllegalArgumentException("Email already in use");
            }

            existing.setFirstName(req.firstName != null ? req.firstName.trim() : existing.getFirstName());
            existing.setLastName(req.lastName != null ? req.lastName.trim() : existing.getLastName());
            // Prefer email as username when present, else mobile (rule 1 — mobile-first).
            existing.setUsername(normalizedEmail != null ? normalizedEmail : trimmedMobile);
            if (normalizedEmail != null) {
                existing.setEmail(normalizedEmail);
            }
            existing.setMobile(trimmedMobile);
            existing.setPasswordHash(passwordEncoder.encode(req.password));
            existing.setLinkedinProfile(req.linkedinProfile);
            existing.setTenantId(tenantId);

            UserStatus newStatus = req.status != null ? req.status : UserStatus.PENDING_VERIFICATION;
            existing.setStatus(newStatus);
            existing.setActive(req.active != null ? req.active : (newStatus == UserStatus.ACTIVE));

            if (req.student) {
                existing.setStudent(true);
            }

            // Ensure default ROLE_USER exists
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
            Set<Role> roles = existing.getRoles() != null ? existing.getRoles() : new HashSet<>();
            roles.add(userRole);
            existing.setRoles(roles);

            User saved = userRepository.save(existing);

            if (req.student) {
                upsertStudentExperience(saved, req, tenantId);
            }

            log.info("User re-signup (pending verification) id={}, email={}, mobile={}, tenantId={}",
                    saved.getId(), saved.getEmail(), saved.getMobile(), tenantId);
            return saved;
        }

        // Create and save user
        User user = new User();
        user.setFirstName(req.firstName != null ? req.firstName.trim() : null);
        user.setLastName(req.lastName != null ? req.lastName.trim() : null);
        // Rule 1 — username is email when provided, else falls back to mobile.
        user.setUsername(normalizedEmail != null ? normalizedEmail : trimmedMobile);
        user.setEmail(normalizedEmail);
        user.setMobile(trimmedMobile);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        UserStatus newStatus = req.status != null ? req.status : UserStatus.PENDING_VERIFICATION;
        user.setStatus(newStatus);
        user.setActive(req.active != null ? req.active : (newStatus == UserStatus.ACTIVE));
        user.setTenantId(tenantId);
        user.setLinkedinProfile(req.linkedinProfile);
        
        // Assign default ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        user.setRoles(Set.of(userRole));

        user.setStudent(req.student);

        User saved = userRepository.save(user);

        if (req.student) {
            upsertStudentExperience(saved, req, tenantId);
        }

        log.info("User signup successful id={}, email={}, tenantId={}", saved.getId(), saved.getEmail(), tenantId);
        return saved;
    }

    @Transactional
    public User adminCreateUser(UUID b2bUnitId, String firstName, String lastName, String email, String mobile, List<UUID> roleIds) {
        String tenantId = currentTenant();
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(email.toLowerCase());
        u.setEmail(email.toLowerCase());
        u.setMobile(mobile);
        u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setActive(true);
        u.setTenantId(tenantId);
        setB2BUnitIfPresent(u, b2bUnitId);
        u.setPasswordNeedsReset(true);
        u.setPasswordSetupToken(UUID.randomUUID().toString());
        u.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        Role r = ensureRole("BUSINESS_ADMIN", "Business Admin", b2bUnitId);
        Set<Role> roles = Optional.ofNullable(u.getRoles()).orElseGet(HashSet::new);
        roles.add(r);
        u.setRoles(roles);
        return userRepository.save(u);
    }

    @Transactional
    public User adminUpdateUser(UUID id, String firstName, String lastName, String mobile, Boolean active, List<UUID> roleIds) {
        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
            
        // Update basic info
        if (firstName != null) {
            user.setFirstName(firstName.isBlank() ? null : firstName.trim());
        }
        
        if (lastName != null) {
            user.setLastName(lastName.isBlank() ? null : lastName.trim());
        }
        
        // Update mobile with validation
        if (mobile != null) {
            String trimmedMobile = mobile.isBlank() ? null : mobile.trim();
            if (trimmedMobile != null && !trimmedMobile.equals(user.getMobile())) {
                userRepository.findByMobile(trimmedMobile)
                    .filter(x -> !x.getId().equals(id))
                    .ifPresent(x -> { 
                        throw new IllegalArgumentException("Mobile number already in use"); 
                    });
                user.setMobile(trimmedMobile);
            }
        }
        
        // Update active status
        if (active != null) {
            user.setActive(active);
        }
        
        // Update roles if provided
        if (roleIds != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            
            // Ensure the user always has at least the ROLE_USER
            if (roles.stream().noneMatch(role -> "ROLE_USER".equals(role.getName()))) {
                Role userRole = getOrCreateRole("ROLE_USER", "Default role for all users", (user.getB2bUnit() != null ? user.getB2bUnit().getId() : null));
                roles.add(userRole);
            }
            
            user.setRoles(roles);
        }
        
        // Update audit fields
        user.setUpdatedDate(Instant.now());
        
        // Get current user for audit
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                user.setUpdatedBy(email);
            }
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Admin updated user id={}, email={}", updatedUser.getId(), updatedUser.getEmail());
        return updatedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String emailOrMobile, String rawPassword) {
        // First try to find by email (hash-based lookup)
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(emailOrMobile);

        // Backward-compat fallback: legacy rows may not have email_hash populated yet.
        // In most flows username is set to the normalized email and remains searchable.
        if (userOpt.isEmpty() && emailOrMobile != null && emailOrMobile.contains("@")) {
            String normalized = emailOrMobile.trim().toLowerCase();
            userOpt = userRepository.findByUsername(normalized);
        }
        
        // If not found by email, try by mobile
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByMobile(emailOrMobile);
        }
        
        // Check if user exists, is active, and password matches
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isActive()) {
                log.warn("Authentication failed: User {} is inactive", emailOrMobile);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Account is not yet active. Please verify OTP.");
            }

            if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
                log.warn("Authentication failed: User {} status is {}", emailOrMobile, user.getStatus());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Account is not yet active. Please verify OTP.");
            }
            
            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                log.warn("Authentication failed: Invalid password for user {}", emailOrMobile);
                return Optional.empty();
            }
            
            log.info("User authenticated successfully id={}, email={}", user.getId(), user.getEmail());
            return Optional.of(user);
        }
        
        log.warn("Authentication failed: User not found with identifier={}", emailOrMobile);
        return Optional.empty();
    }

    @Transactional
    public void updateStatus(@NonNull UUID userId, @NonNull UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(status);
        user.setActive(status == UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        userOpt.ifPresentOrElse(
            user -> log.debug("Found user by email: {}", email),
            () -> log.debug("No user found with email: {}", email)
        );
        return userOpt;
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(@NonNull UUID id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresentOrElse(
            user -> log.debug("Found user by id: {}", id),
            () -> log.debug("No user found with id: {}", id)
        );
        return userOpt;
    }

    @Transactional
    public void incrementTokenVersion(@NonNull UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(UUID b2bUnitId, String q, String role, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
       return userRepository.search(b2bUnitId, query, "BUSINESS_ADMIN", pageable);
    }

    @Transactional
    public User createBusinessAdmin(UUID b2bUnitId, String firstName, String lastName, String email, String mobile) {
        String tenantId = currentTenant();
        
        // Create new user
        User admin = new User();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setUsername(email.toLowerCase());
        admin.setEmail(email.toLowerCase());
        admin.setMobile(mobile);
        admin.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setActive(true);
        admin.setTenantId(tenantId);
        setB2BUnitIfPresent(admin, b2bUnitId);
        admin.setPasswordNeedsReset(true);
        admin.setPasswordSetupToken(UUID.randomUUID().toString());
        admin.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));

        // Ensure ROLE_ADMIN exists for this business
        Role adminRole = ensureRole("ROLE_ADMIN", "Business Administrator", b2bUnitId);
            
        // Assign roles
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        
        // Ensure user also has ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        admin.setRoles(roles);
        User saved = userRepository.save(admin);
        
        log.info("Business admin user created id={}, email={}, b2bUnitId={}, tenantId={}",
            saved.getId(), saved.getEmail(), b2bUnitId, tenantId);
            
        return saved;
    }

    /**
     * UUID-based overload of {@link #assignBusinessAdmin(UUID, String)}.
     * Preferred when the caller has the user's UUID (e.g., from the gateway's
     * {@code X-User-Id} header) to avoid the email→hash→lookup round-trip.
     */
    @Transactional
    public void assignBusinessAdminById(UUID b2bUnitId, UUID userId) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        log.debug("Assigning BUSINESS_ADMIN role to userId={}", userId);

        Role businessAdminRole = ensureRole("ROLE_BUSINESS_ADMIN", "Business Administrator", b2bUnitId);
        Set<Role> roles = new HashSet<>(user.getRoles() != null ? user.getRoles() : Set.of());
        roles.add(businessAdminRole);

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);

        user.setRoles(roles);
        setB2BUnitIfPresent(user, b2bUnitId);

        User saved = userRepository.save(user);
        log.info("Assigned ROLE_BUSINESS_ADMIN to userId={}, b2bUnitId={}, tenantId={}",
            saved.getId(), b2bUnitId, tenantId);
    }

    @Transactional
    public void assignBusinessAdmin(UUID b2bUnitId, String email) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        
        // Find the user by email
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
            
        log.debug("Assigning BUSINESS_ADMIN role to user: {}", email);

        // Ensure ROLE_BUSINESS_ADMIN exists for this business
        Role businessAdminRole = ensureRole("ROLE_BUSINESS_ADMIN", "Business Administrator", b2bUnitId);

        // Get existing roles or create a new set if none exists
        Set<Role> roles = new HashSet<>(user.getRoles());
            
        // Add the business admin role
        roles.add(businessAdminRole);
        
        // Ensure the user has the ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        // Update user roles and business unit
        user.setRoles(roles);
        setB2BUnitIfPresent(user, b2bUnitId);
        
        // Save the updated user
        User saved = userRepository.save(user);
        
        log.info("Assigned ROLE_BUSINESS_ADMIN to user id={}, email={}, b2bUnitId={}, tenantId={}",
            saved.getId(), saved.getEmail(), b2bUnitId, tenantId);

    }

    @SuppressWarnings("null")
	@Transactional
    public Employee inviteEmployee(UUID b2bUnitId, InviteEmployeeRequest req) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        
        // Check if email already exists
        userRepository.findByEmailIgnoreCase(req.getEmail()).ifPresent(u -> { 
            throw new IllegalArgumentException("Email already in use"); 
        });
        
        // Create new employee
        Employee emp = new Employee();
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setUsername(req.getEmail().toLowerCase());
        emp.setEmail(req.getEmail().toLowerCase());
        emp.setMobile(req.getMobile());
        emp.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 8));
        emp.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        emp.setActive(true);
        emp.setTenantId(tenantId);
        setB2BUnitIfPresent(emp, b2bUnitId);
        emp.setPasswordNeedsReset(true);
        emp.setPasswordSetupToken(UUID.randomUUID().toString());
        emp.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        
        // Handle role assignments
        Set<Role> roles = new HashSet<>();
        
        // Add specified roles if any
        if (Objects.nonNull(req.getRoleIds())) {
            Set<Role> specifiedRoles = new HashSet<>(roleRepository.findAllById(req.getRoleIds()));
            roles.addAll(specifiedRoles);
        }
        
        // Ensure the user has at least the ROLE_EMPLOYEE
        Role employeeRole = roleRepository.findByNameAndB2bUnitId("ROLE_EMPLOYEE", b2bUnitId)
            .orElseGet(() -> {
                Role role = new Role("ROLE_EMPLOYEE", "Employee role with basic access", b2bUnitId);
                return roleRepository.save(role);
            });
        roles.add(employeeRole);
        
        // Ensure the user has the ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        emp.setRoles(roles);
        
        // Populate createdBy/updatedBy from current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String actor = auth.getName();
            Object principal = auth.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String emailClaim = jwt.getClaimAsString("email");
                if (emailClaim != null && !emailClaim.isBlank()) {
                    actor = emailClaim;
                }
            }
            if (actor != null && !actor.isBlank()) {
                emp.setCreatedBy(actor);
                emp.setUpdatedBy(actor);
            }
        }
        
        // Save the employee
        Employee savedEmployee = userRepository.save(emp);
        
        log.info("Employee invited successfully id={}, email={}, employeeCode={}, b2bUnitId={}, tenantId={}",
            savedEmployee.getId(), 
            savedEmployee.getEmail(), 
            savedEmployee.getEmployeeCode(),
            b2bUnitId, 
            tenantId);
        
        return savedEmployee;
    }

    /**
     * Helper method to get or create a role if it doesn't exist
     */
    private Role getOrCreateRole(String roleName, String description, UUID b2bUnitId) {
        return roleRepository.findByNameAndB2bUnitId(roleName, b2bUnitId)
            .orElseGet(() -> {
                Role role = new Role(roleName, description, b2bUnitId);
                return roleRepository.save(role);
            });
    }

    @Transactional
    public boolean setupPassword(String token, String newPassword) {
        // Validate input parameters
        if (token == null || token.isBlank()) {
            log.warn("Password setup failed: Token is required");
            return false;
        }
        if (newPassword == null || newPassword.length() < 8) {
            log.warn("Password setup failed: New password must be at least 8 characters long");
            return false;
        }
        
        // Find user by token
        Optional<User> userOpt = userRepository.findByPasswordSetupToken(token);
        if (userOpt.isEmpty()) { log.warn("Password setup failed: Invalid or expired token"); return false; }
        User user = userOpt.get();
        
        // Check if token is expired
        if (user.getPasswordSetupTokenExpires() == null || user.getPasswordSetupTokenExpires().isBefore(Instant.now())) {
            log.warn("Password setup failed: Token expired for user id={}, email={}", user.getId(), user.getEmail());
            return false;
        }
        
        // Update user password and clear reset flags
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordNeedsReset(false);
        user.setPasswordSetupToken(null);
        user.setPasswordSetupTokenExpires(null);
        
        // Update updatedBy if there's an authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String actor = auth.getName();
            if (actor != null && !actor.isBlank()) {
                user.setUpdatedBy(actor);
            }
        }
        
        // Save the updated user
        userRepository.save(user);
        
        log.info("Password setup completed successfully for user id={}, email={}", 
            user.getId(), user.getEmail());
            
        return true;
    }

    @Transactional(readOnly = true)
    public User getUserById(@NonNull UUID userId) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with id: " + userId);
        }
        return user.get();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        return userRepository.findAllById(ids).stream().filter(java.util.Objects::nonNull).toList();
    }
}
