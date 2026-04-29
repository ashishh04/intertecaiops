package com.juviai.user.security.scope;

import com.juviai.user.domain.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Collects every {@link ScopeResolver} bean on the classpath and exposes
 * them by {@link ScopeType}. Each service contributes the resolvers for the
 * scope types it owns, and any module that needs to reason about scopes
 * (ScopedSecurity, RoleAssignmentService, …) injects this registry.
 *
 * <p>Services that don't register a resolver for a given type simply can't
 * resolve / validate assignments at that type — the calls gracefully return
 * {@link Optional#empty()} and {@code false}, and {@code ScopedSecurity} falls
 * back to the DB.</p>
 */
@Component
public class ScopeResolverRegistry {

    private static final Logger log = LoggerFactory.getLogger(ScopeResolverRegistry.class);

    private final Map<ScopeType, ScopeResolver> resolvers = new EnumMap<>(ScopeType.class);

    public ScopeResolverRegistry(List<ScopeResolver> resolverBeans) {
        for (ScopeResolver resolver : resolverBeans) {
            ScopeResolver previous = resolvers.put(resolver.type(), resolver);
            if (previous != null) {
                log.warn("Multiple ScopeResolver beans registered for {}; keeping {} over {}",
                        resolver.type(), resolver.getClass().getSimpleName(),
                        previous.getClass().getSimpleName());
            }
        }
        log.info("ScopeResolverRegistry initialised with {} resolver(s): {}",
                resolvers.size(), resolvers.keySet());
    }

    public Optional<ScopeResolver> find(ScopeType type) {
        return Optional.ofNullable(resolvers.get(type));
    }
}
