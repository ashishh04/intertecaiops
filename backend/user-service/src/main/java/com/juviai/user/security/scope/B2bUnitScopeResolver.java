package com.juviai.user.security.scope;

import com.juviai.user.domain.Role;
import com.juviai.user.domain.ScopeRef;
import com.juviai.user.domain.ScopeType;
import com.juviai.user.organisation.repo.B2BUnitRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Scope resolver for {@link ScopeType#B2B_UNIT}. Lives in user-service
 * where the B2BUnit entity is defined. Delegates to the existing
 * {@code B2BUnitRepository} for existence checks; B2B units are roots, so
 * there is no parent.
 *
 * <p>{@link #inheritableRoles()} enumerates the roles that — if held at the
 * parent B2B unit — implicitly grant admin rights over child scopes
 * (stores, projects, warehouses). This is the single place to change that
 * cascade policy.</p>
 */
@Component
public class B2bUnitScopeResolver implements ScopeResolver {

    private final B2BUnitRepository b2bUnitRepository;

    public B2bUnitScopeResolver(B2BUnitRepository b2bUnitRepository) {
        this.b2bUnitRepository = b2bUnitRepository;
    }

    @Override
    public ScopeType type() {
        return ScopeType.B2B_UNIT;
    }

    @Override
    public boolean exists(UUID scopeId) {
        return scopeId != null && b2bUnitRepository.existsById(scopeId);
    }

    @Override
    public Optional<ScopeRef> parentOf(UUID scopeId) {
        // B2B_UNIT is a top-level scope in our model.
        return Optional.empty();
    }

    @Override
    public Set<String> inheritableRoles() {
        return Set.of(
                Role.ROLE_BUSINESS_ADMIN,
                Role.ROLE_BUSINESS_OWNER,
                "BUSINESS_ADMIN",
                "HR_ADMIN",
                "ROLE_HR_ADMIN"
        );
    }
}
