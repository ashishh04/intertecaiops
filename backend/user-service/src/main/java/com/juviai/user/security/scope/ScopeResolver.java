package com.juviai.user.security.scope;

import com.juviai.user.domain.ScopeRef;
import com.juviai.user.domain.ScopeType;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * SPI that teaches the generic scoped-role machinery about one kind of
 * {@link ScopeType}. Services own the scope types that live in their domain:
 * <ul>
 *   <li>user-service: {@code B2bUnitScopeResolver}, {@code ProjectScopeResolver}</li>
 *   <li>commerce-service: {@code StoreScopeResolver}</li>
 *   <li>fulfillment-service: {@code WarehouseScopeResolver}</li>
 *   <li>... and so on</li>
 * </ul>
 *
 * <p>Adding a new kind of scope means adding a new {@link ScopeType} enum
 * value and one resolver bean — no core code changes required.</p>
 */
public interface ScopeResolver {

    /** Which {@link ScopeType} this resolver handles. */
    ScopeType type();

    /** Does the scope entity actually exist? Used to reject bad assignments. */
    boolean exists(UUID scopeId);

    /**
     * Immediate parent of this scope — e.g. Store's parent is its B2BUnit.
     * Returns {@link Optional#empty()} for root scopes.
     */
    Optional<ScopeRef> parentOf(UUID scopeId);

    /**
     * Roles held at this scope that should "cascade down" to child scopes.
     * For example, being {@code ROLE_BUSINESS_ADMIN} at a B2B unit usually
     * implies admin rights over all of its stores and projects; the B2B-unit
     * resolver would return {@code {ROLE_BUSINESS_ADMIN, ROLE_HR_ADMIN}}.
     *
     * <p>The role check is name-based, case-sensitive. Include both the
     * {@code ROLE_*} and bare forms if both are in circulation.</p>
     *
     * <p>Default: no roles cascade.</p>
     */
    default Set<String> inheritableRoles() {
        return Set.of();
    }
}
