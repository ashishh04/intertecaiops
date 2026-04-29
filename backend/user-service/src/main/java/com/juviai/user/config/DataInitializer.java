package com.juviai.user.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.DependsOn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.juviai.user.domain.Permission;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import com.juviai.user.domain.UserStatus;
import com.juviai.common.tenant.TenantContext;
import com.juviai.user.repo.PermissionRepository;
import com.juviai.user.repo.RoleRepository;
import com.juviai.user.repo.UserRepository;
import com.juviai.user.service.RoleService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * Component that initializes default roles and permissions in the system.
 * Runs automatically on application startup.
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
@DependsOn("aesFieldEncryptor")
public class DataInitializer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);
    

    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${app.admin.seed.email:vamsi.chadaram@juvarya.com}")
    private String seedAdminEmail;

    @Value("${app.admin.seed.password:Skillrat@12345}")
    private String seedAdminPassword;

    @Value("${app.admin.seed.force-password-update:false}")
    private boolean forceSeedAdminPasswordUpdate;

    /**
     * Initialize default data on application startup
     */
    @PostConstruct
    @Transactional
    public void init() {
        log.info("Initializing default roles and permissions...");
        
        // Initialize default system roles
        roleService.initializeDefaultRoles();
        
        // Initialize default permissions
        initializeDefaultPermissions();
        
        // Assign permissions to roles
        assignPermissionsToRoles();

        seedAdminUser();
        
        log.info("Default roles and permissions initialized successfully");
    }

    private void seedAdminUser() {
        if (seedAdminEmail == null || seedAdminEmail.isBlank()) {
            return;
        }

        String normalizedEmail = seedAdminEmail.trim().toLowerCase();
        log.info("Admin seed enabled for email='{}' forcePasswordUpdate={}", normalizedEmail, forceSeedAdminPasswordUpdate);
        User user = userRepository.findByUsername(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                .orElseGet(User::new);
        boolean isNew = user.getId() == null;

        user.setUsername(normalizedEmail);
        user.setEmail(normalizedEmail);
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            user.setFirstName("Admin");
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            user.setLastName("User");
        }
        user.setActive(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordNeedsReset(false);

        if (user.getTenantId() == null || user.getTenantId().isBlank()) {
            String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : "default";
            user.setTenantId(tenantId);
        }

        boolean devLikeProfile = isDevLikeProfile();

        boolean seedPasswordPresent = seedAdminPassword != null && !seedAdminPassword.isBlank();
        boolean missingHash = user.getPasswordHash() == null || user.getPasswordHash().isBlank();
        boolean mismatchedHash = !missingHash && seedPasswordPresent && !passwordEncoder.matches(seedAdminPassword, user.getPasswordHash());

        boolean shouldUpdatePassword = seedPasswordPresent && (
                forceSeedAdminPasswordUpdate
                        || isNew
                        || missingHash
                        || (devLikeProfile && mismatchedHash)
        );
        if (shouldUpdatePassword) {
            user.setPasswordHash(passwordEncoder.encode(seedAdminPassword));
            log.info("Admin seed password updated for email='{}' (isNew={} forcePasswordUpdate={})", normalizedEmail, isNew, forceSeedAdminPasswordUpdate);
        }

        Set<Role> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();

        roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(roles::add);
        roleRepository.findByName("ROLE_ADMIN").ifPresent(roles::add);
        roleRepository.findByName(Role.ROLE_USER).ifPresent(roles::add);

        user.setRoles(roles);
        userRepository.save(user);
    }

    private boolean isDevLikeProfile() {
        boolean devLikeProfile = false;
        try {
            String[] profiles = environment != null ? environment.getActiveProfiles() : new String[0];
            if (profiles.length == 0) {
                // No active profile means "default" profile is being used (typical local dev via IntelliJ)
                devLikeProfile = true;
            } else {
                for (String p : profiles) {
                    if (p == null) continue;
                    String x = p.trim().toLowerCase();
                    if (x.equals("local") || x.equals("dev")) {
                        devLikeProfile = true;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return devLikeProfile;
    }

    /**
     * Initialize default system permissions
     */
    private void initializeDefaultPermissions() {
        // System permissions
        createPermissionIfNotExists("user:read", "View user information");
        createPermissionIfNotExists("user:create", "Create new users");
        createPermissionIfNotExists("user:update", "Update user information");
        createPermissionIfNotExists("user:delete", "Delete users");
        
        // Business permissions
        createPermissionIfNotExists("business:read", "View business information");
        createPermissionIfNotExists("business:create", "Create new businesses");
        createPermissionIfNotExists("business:update", "Update business information");
        createPermissionIfNotExists("business:delete", "Delete businesses");
        
        // Project permissions
        createPermissionIfNotExists("project:read", "View project information");
        createPermissionIfNotExists("project:create", "Create new projects");
        createPermissionIfNotExists("project:update", "Update project information");
        createPermissionIfNotExists("project:delete", "Delete projects");
        
        // Role management permissions
        createPermissionIfNotExists("role:read", "View roles and permissions");
        createPermissionIfNotExists("role:assign", "Assign roles to users");
        createPermissionIfNotExists("role:manage", "Create, update, and delete roles");
        
        // Other permissions
        createPermissionIfNotExists("settings:manage", "Manage system settings");
        createPermissionIfNotExists("reports:view", "View system reports");
    }
    
    /**
     * Assign permissions to default roles
     */
    private void assignPermissionsToRoles() {
        // Admin role gets all permissions
        assignPermissionsToRole(Role.ROLE_ADMIN, Arrays.asList(
            "user:read", "user:create", "user:update", "user:delete",
            "business:read", "business:create", "business:update", "business:delete",
            "project:read", "project:create", "project:update", "project:delete",
            "role:read", "role:assign", "role:manage",
            "settings:manage", "reports:view"
        ));

        assignPermissionsToRole("ROLE_ADMIN", Arrays.asList(
            "user:read", "user:create", "user:update", "user:delete",
            "business:read", "business:create", "business:update", "business:delete",
            "project:read", "project:create", "project:update", "project:delete",
            "role:read", "role:assign", "role:manage",
            "settings:manage", "reports:view"
        ));
        
        // Business owner role gets business and project management permissions
        assignPermissionsToRole(Role.ROLE_BUSINESS_OWNER, Arrays.asList(
            "user:read", "user:create", "user:update",
            "business:read", "business:update",
            "project:read", "project:create", "project:update", "project:delete",
            "role:read", "role:assign",
            "reports:view"
        ));
        
        // Project manager role gets project management permissions
        assignPermissionsToRole(Role.ROLE_PROJECT_MANAGER, Arrays.asList(
            "user:read",
            "project:read", "project:update",
            "role:read"
        ));
        
        // Team lead role gets limited project permissions
        assignPermissionsToRole(Role.ROLE_TEAM_LEAD, Arrays.asList(
            "user:read",
            "project:read"
        ));
        
        // Regular user role gets basic read permissions
        assignPermissionsToRole(Role.ROLE_USER, Arrays.asList(
            "user:read",
            "project:read"
        ));
    }
    
    /**
     * Helper method to create a permission if it doesn't exist
     */
    private void createPermissionIfNotExists(String name, String description) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.debug("Created permission: {}", name);
        }
    }
    
    /**
     * Helper method to assign permissions to a role
     */
    private void assignPermissionsToRole(String roleName, List<String> permissionNames) {
        // Fetch all roles matching this name (handles accidental duplicates gracefully)
        List<Role> roles = roleRepository.findAllByName(roleName);
        if (roles == null || roles.isEmpty()) {
            log.warn("No role found with name '{}' while assigning permissions", roleName);
            return;
        }

        Set<Permission> permissions = new HashSet<>();
        for (String permissionName : permissionNames) {
            permissionRepository.findByName(permissionName).ifPresent(permissions::add);
        }

        for (Role role : roles) {
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
        log.debug("Assigned {} permissions to {} role record(s) for name: {}", permissions.size(), roles.size(), roleName);
    }
}
