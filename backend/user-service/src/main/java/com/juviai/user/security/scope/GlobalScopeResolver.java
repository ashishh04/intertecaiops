package com.juviai.user.security.scope;

import com.juviai.user.domain.ScopeRef;
import com.juviai.user.domain.ScopeType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Trivial resolver for {@link ScopeType#GLOBAL} — the scope id is always
 * {@code null} and always considered "present". Having a resolver for the
 * global scope keeps {@code RoleAssignmentService} uniform (no special
 * casing for GLOBAL in validation).
 */
@Component
public class GlobalScopeResolver implements ScopeResolver {

    @Override
    public ScopeType type() {
        return ScopeType.GLOBAL;
    }

    @Override
    public boolean exists(UUID scopeId) {
        // GLOBAL is always "present" and id must be null
        return scopeId == null;
    }

    @Override
    public Optional<ScopeRef> parentOf(UUID scopeId) {
        return Optional.empty();
    }
}
