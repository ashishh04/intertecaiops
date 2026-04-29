package com.juviai.user.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a role in the system that can be assigned to users.
 * Roles can be system-wide or specific to a business unit.
 */
@Setter
@Getter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    /** Legacy constant — use {@link #ROLE_BUSINESS_ADMIN} for new code. */
    public static final String ROLE_BUSINESS_OWNER = "BUSINESS_ADMIN";
    /** Assigned to users who are admins of a B2BUnit (restaurants, companies, etc.). */
    public static final String ROLE_BUSINESS_ADMIN = "ROLE_BUSINESS_ADMIN";
    public static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    public static final String ROLE_TEAM_LEAD = "TEAM_LEAD";
    public static final String ROLE_DEVELOPER = "DEVELOPER";

    // Getters and Setters
    @Column(nullable = false, length = 64, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "b2b_unit_id")
    private UUID b2bUnitId;

    @ManyToMany(mappedBy = "roles")
    @JsonBackReference("user-roles")
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonBackReference("role-permissions")
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Functional modules / applications this role is applicable to
     * (HRMS, ECOMMERCE, PROJECT_MANAGEMENT, ...). Used to filter which
     * roles can be assigned to a user in a given module UI — e.g. when
     * assigning roles in the HRMS application only roles that contain
     * {@link RoleModule#HRMS} in this set should be offered.
     *
     * <p>Stored in a join table {@code role_modules(role_id, module)}.</p>
     */
    @ElementCollection(targetClass = RoleModule.class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "role_modules",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "module", length = 64, nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<RoleModule> modules = EnumSet.noneOf(RoleModule.class);

    /**
     * The scope types at which this role may be assigned. For example
     * {@code ROLE_STORE_ADMIN.applicableScopes = {STORE}},
     * {@code ROLE_BUSINESS_ADMIN.applicableScopes = {B2B_UNIT}},
     * {@code ROLE_USER.applicableScopes = {GLOBAL}}. Used by
     * {@code RoleAssignmentService} to reject nonsensical assignments
     * (e.g. {@code ROLE_STORE_ADMIN} at a PROJECT scope) and by the UI
     * to narrow down the "assign role" dropdown on a scope-specific page.
     *
     * <p>Stored in a join table {@code role_scopes(role_id, scope_type)}.</p>
     */
    @ElementCollection(targetClass = ScopeType.class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "role_scopes",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "scope_type", length = 64, nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ScopeType> applicableScopes = EnumSet.noneOf(ScopeType.class);

    // Constructors
    public Role() {
        // Default constructor
    }

    public Role(String name, String description, UUID b2bUnitId) {
        this.name = name;
        this.description = description;
        this.b2bUnitId = b2bUnitId;
    }

    public Role(String name, String description, UUID b2bUnitId, Set<RoleModule> modules) {
        this.name = name;
        this.description = description;
        this.b2bUnitId = b2bUnitId;
        if (modules != null && !modules.isEmpty()) {
            this.modules = EnumSet.copyOf(modules);
        }
    }

    // Helper methods for module management
    public void addModule(RoleModule module) {
        if (module != null) {
            if (this.modules == null) {
                this.modules = EnumSet.noneOf(RoleModule.class);
            }
            this.modules.add(module);
        }
    }

    public void removeModule(RoleModule module) {
        if (module != null && this.modules != null) {
            this.modules.remove(module);
        }
    }

    public boolean hasModule(RoleModule module) {
        return module != null && this.modules != null && this.modules.contains(module);
    }

    // Helper methods for applicable-scope management
    public void addApplicableScope(ScopeType scope) {
        if (scope != null) {
            if (this.applicableScopes == null) {
                this.applicableScopes = EnumSet.noneOf(ScopeType.class);
            }
            this.applicableScopes.add(scope);
        }
    }

    public void removeApplicableScope(ScopeType scope) {
        if (scope != null && this.applicableScopes != null) {
            this.applicableScopes.remove(scope);
        }
    }

    public boolean isApplicableAt(ScopeType scope) {
        return scope != null
                && this.applicableScopes != null
                && this.applicableScopes.contains(scope);
    }

    // Helper methods for permission management
    public void addPermission(Permission permission) {
        if (permission != null) {
            if (this.permissions == null) {
                this.permissions = new HashSet<>();
            }
            this.permissions.add(permission);
            if (permission.getRoles() != null) {
                permission.getRoles().add(this);
            }
        }
    }
    
    public void removePermission(Permission permission) {
        if (permission != null && this.permissions != null) {
            this.permissions.remove(permission);
            if (permission.getRoles() != null) {
                permission.getRoles().remove(this);
            }
        }
    }
    
    // Builder pattern methods
    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public static class RoleBuilder {
        private UUID id;
        private String name;
        private String description;
        private UUID b2bUnitId;
        private Set<User> users = new HashSet<>();
        private Set<Permission> permissions = new HashSet<>();
        private Set<RoleModule> modules = EnumSet.noneOf(RoleModule.class);
        private Set<ScopeType> applicableScopes = EnumSet.noneOf(ScopeType.class);

        public RoleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RoleBuilder b2bUnitId(UUID b2bUnitId) {
            this.b2bUnitId = b2bUnitId;
            return this;
        }

        public RoleBuilder users(Set<User> users) {
            this.users = users;
            return this;
        }

        public RoleBuilder permissions(Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public RoleBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public RoleBuilder modules(Set<RoleModule> modules) {
            this.modules = (modules == null || modules.isEmpty())
                    ? EnumSet.noneOf(RoleModule.class)
                    : EnumSet.copyOf(modules);
            return this;
        }

        public RoleBuilder module(RoleModule module) {
            if (module != null) {
                this.modules.add(module);
            }
            return this;
        }

        public RoleBuilder applicableScopes(Set<ScopeType> scopes) {
            this.applicableScopes = (scopes == null || scopes.isEmpty())
                    ? EnumSet.noneOf(ScopeType.class)
                    : EnumSet.copyOf(scopes);
            return this;
        }

        public RoleBuilder applicableScope(ScopeType scope) {
            if (scope != null) {
                this.applicableScopes.add(scope);
            }
            return this;
        }

        public Role build() {
            Role role = new Role();
            if (id != null) role.setId(id);
            role.setName(name);
            role.setDescription(description);
            role.setB2bUnitId(b2bUnitId);
            if (users != null) role.setUsers(users);
            if (permissions != null) role.setPermissions(permissions);
            if (modules != null) role.setModules(modules);
            if (applicableScopes != null) role.setApplicableScopes(applicableScopes);
            return role;
        }
    }

    // User management methods
    public void addUser(User user) {
        if (user != null) {
            if (this.users == null) {
                this.users = new HashSet<>();
            }
            this.users.add(user);
            if (user.getRoles() != null) {
                user.getRoles().add(this);
            }
        }
    }

    public void removeUser(User user) {
        if (user != null && this.users != null) {
            this.users.remove(user);
            if (user.getRoles() != null) {
                user.getRoles().remove(this);
            }
        }
    }

    // Object overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(getId(), role.getId()) &&
               Objects.equals(name, role.name) &&
               Objects.equals(b2bUnitId, role.b2bUnitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), name, b2bUnitId);
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", b2bUnitId=" + b2bUnitId +
                ", modules=" + modules +
                '}';
    }
}
