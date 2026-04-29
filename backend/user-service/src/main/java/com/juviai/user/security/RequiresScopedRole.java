package com.juviai.user.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method-level shorthand for
 * {@code @PreAuthorize("@scopedSecurity.has(<role>, <type>, <id>)")}.
 *
 * <p>Since Java annotation attributes cannot be referenced from the SpEL
 * expression of a meta-annotation, the actual role/scope values must still
 * be passed explicitly via {@code @PreAuthorize} on the method, or the
 * method must expose parameters named {@code roleName}, {@code scopeType}
 * and {@code scopeId} (captured by SpEL argument binding). The typical
 * usage is therefore one of:</p>
 *
 * <pre>
 * // Explicit — preferred for fixed role/scope combos:
 * &#64;PreAuthorize("@scopedSecurity.has('ROLE_STORE_ADMIN', T(com.juviai.user.domain.ScopeType).STORE, #storeId)")
 * public void onlyStoreAdmin(UUID storeId) { ... }
 *
 * // Parameter-driven:
 * &#64;RequiresScopedRole
 * public void generic(String roleName, ScopeType scopeType, UUID scopeId) { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@PreAuthorize("@scopedSecurity.has(#roleName, #scopeType, #scopeId)")
public @interface RequiresScopedRole {
}
