package com.juviai.user.security.scope;

import com.juviai.user.domain.ScopeRef;
import com.juviai.user.domain.ScopeType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Scope resolver for {@link ScopeType#TENANT}. Tenants in our model are
 * addressed by string id (see {@code tenant_id} columns), not UUID, but the
 * {@link ScopeResolver} contract is UUID-based. For now, this resolver only
 * enforces that a TENANT assignment carries a non-null scope id — ops will
 * not typically issue TENANT-scoped assignments directly; GLOBAL or B2B_UNIT
 * are the common choices.
 */
@Component
public class TenantScopeResolver implements ScopeResolver {

    @Override
    public ScopeType type() {
        return ScopeType.TENANT;
    }

    @Override
    public boolean exists(UUID scopeId) {
        // We don't maintain a tenants table in user-service; presence of a
        // non-null id is the best we can do without a cross-cutting lookup.
        return scopeId != null;
    }

    @Override
    public Optional<ScopeRef> parentOf(UUID scopeId) {
        return Optional.empty();
    }
}
