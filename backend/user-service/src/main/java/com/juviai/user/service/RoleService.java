package com.juviai.user.service;

import com.juviai.common.exception.ResourceNotFoundException;
import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.Permission;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.RoleModule;
import com.juviai.user.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing roles and role assignments.
 */
@Service
@RequiredArgsConstructor
public class RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;
    @Value("${app.roles.init.enabled:true}")
    private boolean initRolesEnabled;
    
    // Default system roles
    public static final List<String> DEFAULT_ROLES = Arrays.asList(
        Role.ROLE_ADMIN,
        "ROLE_ADMIN",
        Role.ROLE_USER,
        Role.ROLE_BUSINESS_OWNER,
        Role.ROLE_PROJECT_MANAGER,
        Role.ROLE_TEAM_LEAD,
        Role.ROLE_DEVELOPER
    );

    /**
     * Initialize default system roles if they don't exist.
     *
     * <p>Each default role is also tagged with the {@link RoleModule}(s)
     * it belongs to so that module-aware admin UIs only show the roles
     * relevant to their context. Universal roles (ADMIN / ROLE_USER) are
     * tagged with every module.</p>
     */
    @Transactional
    public void initializeDefaultRoles() {
        if (!initRolesEnabled) {
            log.info("Default role initialization is disabled via property 'app.roles.init.enabled=false'. Skipping.");
            return;
        }
        String tenantId = getCurrentTenantId();

        Set<String> existingRoleNames = roleRepository.findByNameIn(DEFAULT_ROLES).stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        for (String roleName : DEFAULT_ROLES) {
            if (!existingRoleNames.contains(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription("System default role: " + roleName);
                role.setTenantId(tenantId);
                role.setModules(defaultModulesFor(roleName));
                roleRepository.save(role);
                log.info("Created default role: {} with modules {}", roleName, role.getModules());
            }
        }
    }

    /**
     * Pick the default module set for a well-known role name. Mirrors the
     * backfill logic in the V20260418__add_role_modules Flyway migration.
     */
    private Set<RoleModule> defaultModulesFor(String roleName) {
        if (roleName == null) {
            return EnumSet.noneOf(RoleModule.class);
        }
        switch (roleName) {
            case Role.ROLE_USER:
            case Role.ROLE_ADMIN:
            case "ROLE_ADMIN":
                return EnumSet.allOf(RoleModule.class);
            case Role.ROLE_BUSINESS_OWNER:
            case Role.ROLE_BUSINESS_ADMIN:
                return EnumSet.of(RoleModule.ECOMMERCE);
            case Role.ROLE_PROJECT_MANAGER:
            case Role.ROLE_TEAM_LEAD:
            case Role.ROLE_DEVELOPER:
                return EnumSet.of(RoleModule.PROJECT_MANAGEMENT);
            default:
                if (roleName.startsWith("HR_") || roleName.startsWith("ROLE_HR_")) {
                    return EnumSet.of(RoleModule.HRMS);
                }
                return EnumSet.allOf(RoleModule.class);
        }
    }

    /**
     * Create a new role
     */
    @Transactional
    public Role createRole(Role role) {
        String tenantId = getCurrentTenantId();
        
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
        }
        
        role.setTenantId(tenantId);
        return roleRepository.save(role);
    }
    
    /**
     * Create a new business-specific role
     */
    @Transactional
    public Role createBusinessRole(UUID b2bUnitId, String name, String description) {
        String tenantId = getCurrentTenantId();
        
        roleRepository.findByNameAndB2bUnitId(name, b2bUnitId)
            .ifPresent(r -> { 
                throw new IllegalArgumentException("Role " + name + " already exists for this business"); 
            });
            
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setB2bUnitId(b2bUnitId);
        role.setTenantId(tenantId);
        
        return roleRepository.save(role);
    }

    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public Role getRoleById(@NonNull UUID roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }
    
    /**
     * Get role by name
     */
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    /**
     * List all roles for a specific business unit
     */
    @Transactional(readOnly = true)
    public List<Role> getRolesByBusiness(UUID b2bUnitId) {
        return roleRepository.findByB2bUnitId(b2bUnitId);
    }
    
    /**
     * List all system roles (not specific to any business)
     */
    @Transactional(readOnly = true)
    public List<Role> getSystemRoles(UUID b2bUnitId) {
        List<String> excludedRoles = List.of("ADMIN", "BUSINESS_ADMIN");
        return roleRepository.findByB2bUnitIdAndNameNotIn(b2bUnitId, excludedRoles);
    }
    
    /**
     * List all available roles for a business (both system and business-specific)
     */
    @Transactional(readOnly = true)
    public List<Role> getAvailableRolesForBusiness(UUID b2bUnitId) {
        return roleRepository.findAvailableRolesForBusiness(b2bUnitId);
    }

    /**
     * List all roles tagged with a specific module (e.g. show only HRMS
     * roles in the HRMS application). Returns both system-wide roles and
     * business-scoped roles that belong to the module.
     */
    @Transactional(readOnly = true)
    public List<Role> getRolesByModule(@NonNull RoleModule module) {
        return roleRepository.findByModule(module);
    }

    /**
     * List all roles tagged with a specific module and scoped to either
     * the given business unit or to the system (null b2bUnitId). This is
     * the preferred query when populating a module-aware admin UI.
     */
    @Transactional(readOnly = true)
    public List<Role> getRolesByModuleForBusiness(@NonNull RoleModule module, UUID b2bUnitId) {
        return roleRepository.findByModuleAndBusiness(module, b2bUnitId);
    }
    
    /**
     * Get roles for a specific user
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(UUID userId) {
        return roleRepository.findRolesByUserId(userId);
    }
    
    /**
     * Update an existing role
     */
    @Transactional
    public Role updateRole(@NonNull UUID roleId, String name, String description) {
        Role role = getRoleById(roleId);
        
        if (DEFAULT_ROLES.contains(role.getName())) {
            throw new UnsupportedOperationException("Cannot modify system default roles");
        }
        
        role.setName(name);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }
    
    /**
     * Delete a role
     */
    @Transactional
    public void deleteRole(@NonNull UUID roleId) {
        Role role = getRoleById(roleId);
        
        if (DEFAULT_ROLES.contains(role.getName())) {
            throw new UnsupportedOperationException("Cannot delete system default roles");
        }
        
        // Check if role is assigned to any users
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to users");
        }
        
        roleRepository.delete(role);
    }
    
    /**
     * Check if a user has a specific role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, String roleName) {
        return getUserRoles(userId).stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Check if a user has any of the specified roles
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID userId, List<String> roleNames) {
        Set<String> userRoles = getUserRoles(userId).stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
            
        return roleNames.stream().anyMatch(userRoles::contains);
    }
    
    /**
     * Get all permissions for a specific role
     */
    @Transactional(readOnly = true)
    public Set<String> getRolePermissions(String roleName) {
        return roleRepository.findByName(roleName)
            .map(role -> role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }
    
    private String getCurrentTenantId() {
        return Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    }
}
