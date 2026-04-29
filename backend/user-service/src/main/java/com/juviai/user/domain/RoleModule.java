package com.juviai.user.domain;

/**
 * Identifies the application / functional area a {@link Role} belongs to.
 *
 * <p>A single role can belong to multiple modules (e.g. {@code ROLE_USER} is
 * applicable across every module), so {@link Role#getModules()} is modelled as
 * a {@link java.util.Set}. This lets the UI ask questions like "which roles
 * are valid in the HRMS module?" and only show those when an admin is
 * assigning roles inside that module.</p>
 */
public enum RoleModule {
    /** Human Resources module (employees, payroll, leaves, HR admins). */
    HRMS,

    /** Commerce / storefront module (stores, vendors, business admins). */
    ECOMMERCE,

    /** Project & task management module (project managers, team leads, developers). */
    PROJECT_MANAGEMENT
}
