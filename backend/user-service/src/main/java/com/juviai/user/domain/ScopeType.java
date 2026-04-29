package com.juviai.user.domain;

/**
 * Identifies the kind of entity a {@link RoleAssignment} is attached to.
 *
 * <p>Together with a {@code scopeId} (UUID) this forms the "scope" at which
 * a role is granted. For example a user can be granted {@code ROLE_STORE_ADMIN}
 * at {@code (STORE, <store-uuid>)} or {@code ROLE_BUSINESS_ADMIN} at
 * {@code (B2B_UNIT, <b2b-uuid>)}.</p>
 *
 * <p>Adding a new kind of scope (warehouse, campus, ...) costs a new enum
 * value plus a new {@code ScopeResolver} bean — no schema change.</p>
 */
public enum ScopeType {
    /** System-wide scope; {@code scopeId} is {@code null}. */
    GLOBAL,

    /** A whole tenant/organisation; {@code scopeId} is the tenant UUID. */
    TENANT,

    /** A B2B unit (the top-level business entity). */
    B2B_UNIT,

    /** A single store under a B2B unit (commerce). */
    STORE,

    /** A project (project-service). */
    PROJECT,

    /** A warehouse / fulfillment node. */
    WAREHOUSE,

    /** A department inside a B2B unit (HRMS). */
    DEPARTMENT,

    /** A team (cross-cutting, smaller than a department). */
    TEAM
}
