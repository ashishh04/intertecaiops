package com.juviai.user.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import com.juviai.user.repo.RoleRepository;
import com.juviai.user.repo.UserRepository;
import com.juviai.user.web.dto.InviteEmployeeRequest;
import com.juviai.user.web.dto.SignupRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void signup_setsNormalizedFields_assignsRoleUser_andSaves() {
        TenantContext.setTenantId("t1");

        SignupRequest req = new SignupRequest();
        req.firstName = " John ";
        req.lastName = " Doe ";
        req.email = "John.Doe@Example.com";
        req.mobile = " 999 ";
        req.password = "password123";

        Role roleUser = new Role("ROLE_USER", "Default", null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("hash");

        UUID id = UUID.randomUUID();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(id);
            return u;
        });

        User saved = userService.signup(req);

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(saved.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(saved.getMobile()).isEqualTo("999");
        assertThat(saved.getTenantId()).isEqualTo("t1");
        assertThat(saved.getPasswordHash()).isEqualTo("hash");
        // Default signup status is PENDING_VERIFICATION, so active=false until verified
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getRoles()).containsExactly(roleUser);
    }

    @Test
    void signup_mobileOnly_usesMobileAsUsername_andAssignsRoleUser() {
        // CLAUDE.md rule 1 — users register with mobile first; email is optional.
        TenantContext.setTenantId("t1");

        SignupRequest req = new SignupRequest();
        req.firstName = "Jane";
        req.lastName = "Smith";
        req.mobile = "9876543210";
        req.password = "password123";
        // Deliberately no email — rule 1 says this must be accepted.

        Role roleUser = new Role("ROLE_USER", "Default", null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        User saved = userService.signup(req);

        assertThat(saved.getEmail())
                .as("email is optional when mobile-only — stored as null")
                .isNull();
        assertThat(saved.getUsername())
                .as("rule 1 — username falls back to mobile when email absent")
                .isEqualTo("9876543210");
        assertThat(saved.getMobile()).isEqualTo("9876543210");
        assertThat(saved.getRoles()).containsExactly(roleUser);
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void signup_throwsWhenMobileMissing() {
        // Rule 1 — mobile is now the required primary identifier.
        SignupRequest req = new SignupRequest();
        req.firstName = "NoMobile";
        req.lastName = "User";
        req.email = "nomobile@example.com";
        // No mobile — must be rejected.
        req.password = "password123";

        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mobile number is required");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_throwsIfRoleUserMissing() {
        SignupRequest req = new SignupRequest();
        req.firstName = "John";
        req.lastName = "Doe";
        req.email = "john@example.com";
        req.mobile = "9999999999"; // rule 1 — mobile is required
        req.password = "password123";

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ROLE_USER");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_returnsUser_whenFoundByEmail_active_andPasswordMatches() {
        User u = new User();
        u.setActive(true);
        u.setPasswordHash("hash");

        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);

        Optional<User> result = userService.authenticate("a@b.com", "pw");

        assertThat(result).contains(u);
    }

    @Test
    void authenticate_triesMobile_whenEmailNotFound() {
        User u = new User();
        u.setActive(true);
        u.setPasswordHash("hash");

        when(userRepository.findByEmailIgnoreCase("999")).thenReturn(Optional.empty());
        when(userRepository.findByMobile("999")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);

        Optional<User> result = userService.authenticate("999", "pw");

        assertThat(result).contains(u);
    }

    @Test
    void authenticate_throwsForbidden_whenInactive() {
        User u = new User();
        u.setActive(false);
        u.setPasswordHash("hash");

        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(u));

        // Service throws 403 for inactive accounts instead of returning empty
        assertThatThrownBy(() -> userService.authenticate("a@b.com", "pw"))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Account is not yet active");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_returnsEmpty_whenPasswordDoesNotMatch() {
        User u = new User();
        u.setActive(true);
        u.setPasswordHash("hash");

        when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(false);

        assertThat(userService.authenticate("a@b.com", "pw")).isEmpty();
    }

    @Test
    void incrementTokenVersion_incrementsAndSaves() {
        UUID id = UUID.randomUUID();
        User u = new User();
        u.setId(id);
        u.setTokenVersion(3);

        when(userRepository.findById(id)).thenReturn(Optional.of(u));

        userService.incrementTokenVersion(id);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getTokenVersion()).isEqualTo(4);
    }

    @Test
    void adminUpdateUser_throwsWhenIdNull() {
        assertThatThrownBy(() -> userService.adminUpdateUser(null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID");
    }

    @Test
    void adminUpdateUser_throwsWhenMobileAlreadyInUseByAnotherUser() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setMobile("111");

        User other = new User();
        other.setId(UUID.randomUUID());

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.findByMobile("222")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.adminUpdateUser(id, null, null, "222", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mobile number already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setupPassword_returnsFalse_whenTokenMissingOrPasswordTooShort() {
        assertThat(userService.setupPassword(null, "password123")).isFalse();
        assertThat(userService.setupPassword("  ", "password123")).isFalse();
        assertThat(userService.setupPassword("t", "short")).isFalse();

        verify(userRepository, never()).findByPasswordSetupToken(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setupPassword_returnsFalse_whenTokenNotFound() {
        when(userRepository.findByPasswordSetupToken("t1")).thenReturn(Optional.empty());

        assertThat(userService.setupPassword("t1", "password123")).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setupPassword_returnsFalse_whenTokenExpired() {
        User u = new User();
        u.setPasswordSetupToken("t1");
        u.setPasswordSetupTokenExpires(Instant.now().minus(1, ChronoUnit.DAYS));

        when(userRepository.findByPasswordSetupToken("t1")).thenReturn(Optional.of(u));

        assertThat(userService.setupPassword("t1", "password123")).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setupPassword_updatesUserAndClearsToken_whenValid() {
        User u = new User();
        u.setPasswordSetupToken("t1");
        u.setPasswordSetupTokenExpires(Instant.now().plus(1, ChronoUnit.DAYS));
        u.setPasswordNeedsReset(true);
        u.setPasswordHash("old");

        when(userRepository.findByPasswordSetupToken("t1")).thenReturn(Optional.of(u));
        when(passwordEncoder.encode("password123")).thenReturn("newHash");

        boolean result = userService.setupPassword("t1", "password123");

        assertThat(result).isTrue();
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("newHash");
        assertThat(saved.isPasswordNeedsReset()).isFalse();
        assertThat(saved.getPasswordSetupToken()).isNull();
        assertThat(saved.getPasswordSetupTokenExpires()).isNull();
    }

    @Test
    void inviteEmployee_throwsWhenEmailAlreadyExists() {
        UUID b2bUnitId = UUID.randomUUID();

        InviteEmployeeRequest req = new InviteEmployeeRequest();
        req.setEmail("e@example.com");

        when(userRepository.findByEmailIgnoreCase("e@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.inviteEmployee(b2bUnitId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void inviteEmployee_createsEmployeeRoleIfMissing_andAssignsRoleUser() {
        TenantContext.setTenantId("tenant-x");
        UUID b2bUnitId = UUID.randomUUID();

        InviteEmployeeRequest req = new InviteEmployeeRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("Emp@Example.com");
        req.setMobile("999");
        req.setRoleIds(List.of());

        when(userRepository.findByEmailIgnoreCase("Emp@Example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("pwHash");

        Role roleEmployee = new Role("ROLE_EMPLOYEE", "Employee", b2bUnitId);
        when(roleRepository.findByNameAndB2bUnitId("ROLE_EMPLOYEE", b2bUnitId)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(roleEmployee);

        Role roleUser = new Role("ROLE_USER", "Default", null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        when(userRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        Employee saved = userService.inviteEmployee(b2bUnitId, req);

        assertThat(saved.getEmail()).isEqualTo("emp@example.com");
        assertThat(saved.getUsername()).isEqualTo("emp@example.com");
        assertThat(saved.isPasswordNeedsReset()).isTrue();
        assertThat(saved.getPasswordSetupToken()).isNotBlank();
        assertThat(saved.getPasswordSetupTokenExpires()).isNotNull();
        assertThat(saved.getEmployeeCode()).startsWith("EMP-");
        assertThat(saved.getRoles()).contains(roleEmployee, roleUser);

        verify(roleRepository).findByNameAndB2bUnitId(eq("ROLE_EMPLOYEE"), eq(b2bUnitId));
        verify(roleRepository).findByName(eq("ROLE_USER"));
        verify(userRepository).save(any(Employee.class));
    }

    @Test
    void searchUsers_alwaysUsesBusinessAdminRoleInRepositoryCall() {
        userService.searchUsers(null, "q", "ROLE_USER", org.springframework.data.domain.PageRequest.of(0, 10));
        verify(userRepository).search(eq(null), eq("q"), eq("BUSINESS_ADMIN"), any());
    }

    @Test
    void getUsersByIds_returnsEmpty_whenInputNullOrEmpty() {
        assertThat(userService.getUsersByIds(null)).isEmpty();
        assertThat(userService.getUsersByIds(List.of())).isEmpty();
    }

    @Test
    void getUsersByIds_filtersOutNulls() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        User u1 = new User();
        u1.setId(a);

        when(userRepository.findAllById(List.of(a, b))).thenReturn(Arrays.asList(u1, null));

        assertThat(userService.getUsersByIds(List.of(a, b))).containsExactly(u1);
    }

    @Test
    void assignBusinessAdminById_addsBusinessAdminAndUserRole_andLinksB2bUnit() {
        // CLAUDE.md rule 2 — the user who creates a B2BUnit becomes its admin.
        // B2BUnitService.selfSignup() calls this method with the caller's UUID
        // after persisting the unit; this test pins the role/link contract.
        UUID b2bUnitId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("creator@example.com");
        user.setRoles(new java.util.HashSet<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Tenant-scoped BUSINESS_ADMIN role — ensureRole is expected to find it.
        Role businessAdmin = new Role("ROLE_BUSINESS_ADMIN", "Business Administrator", b2bUnitId);
        when(roleRepository.findByNameAndB2bUnitId("ROLE_BUSINESS_ADMIN", b2bUnitId))
                .thenReturn(Optional.of(businessAdmin));

        Role roleUser = new Role("ROLE_USER", "Default", null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.assignBusinessAdminById(b2bUnitId, userId);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getRoles())
                .as("rule 2 — both BUSINESS_ADMIN (scoped) and ROLE_USER must be granted")
                .contains(businessAdmin, roleUser);
        assertThat(saved.getB2bUnit())
                .as("rule 2 — user must be linked to the B2BUnit they created")
                .isNotNull();
        assertThat(saved.getB2bUnit().getId()).isEqualTo(b2bUnitId);
    }

    @Test
    void assignBusinessAdmin_addsRolesAndSaves() {
        UUID b2bUnitId = UUID.randomUUID();

        User user = new User();
        user.setEmail("x@example.com");
        user.setRoles(Set.of());

        when(userRepository.findByEmailIgnoreCase("x@example.com")).thenReturn(Optional.of(user));

        Role businessAdmin = new Role("ROLE_BUSINESS_ADMIN", "Business", b2bUnitId);
        when(roleRepository.findByNameAndB2bUnitId("ROLE_BUSINESS_ADMIN", b2bUnitId)).thenReturn(Optional.of(businessAdmin));

        Role roleUser = new Role("ROLE_USER", "Default", null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.assignBusinessAdmin(b2bUnitId, "x@example.com");

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).contains(businessAdmin, roleUser);
        assertThat(userCaptor.getValue().getB2bUnit()).isNotNull();
        assertThat(userCaptor.getValue().getB2bUnit().getId()).isEqualTo(b2bUnitId);
    }
}
