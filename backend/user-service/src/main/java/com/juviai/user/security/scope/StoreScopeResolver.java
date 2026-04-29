package com.juviai.user.security.scope;

import com.juviai.user.client.StoreClient;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.ScopeRef;
import com.juviai.user.domain.ScopeType;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Scope resolver for {@link ScopeType#STORE}. The Store entity lives in
 * commerce-service, so this resolver delegates to {@link StoreClient} via
 * Feign for both existence checks and the parent-B2B-unit lookup.
 *
 * <p>Parent hierarchy: {@code STORE → B2B_UNIT} — a store is always owned by
 * a B2B unit (or standalone, in which case there's no parent to cascade
 * through). The B2B-unit-scoped admin roles declared by
 * {@link B2bUnitScopeResolver#inheritableRoles()} automatically apply to
 * every store under that unit thanks to the cascade walk in
 * {@code RoleAssignmentService.hasEffectiveRole}.</p>
 *
 * <p>Roles granted directly at STORE scope and declared here as "inheritable"
 * (store-admin, store-manager) do not cascade upward — they are listed so
 * that if a future resolver adds a child scope (e.g. AISLE, SECTION), those
 * child resolvers can walk up through a store and pick them up.</p>
 */
@Component
public class StoreScopeResolver implements ScopeResolver {

    private static final Logger log = LoggerFactory.getLogger(StoreScopeResolver.class);

    /** Store-admin role name — kept here because {@link Role} does not define it yet. */
    public static final String ROLE_STORE_ADMIN = "ROLE_STORE_ADMIN";

    /** Store-manager role name. */
    public static final String ROLE_STORE_MANAGER = "ROLE_STORE_MANAGER";

    private final StoreClient storeClient;

    public StoreScopeResolver(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    @Override
    public ScopeType type() {
        return ScopeType.STORE;
    }

    @Override
    public boolean exists(UUID scopeId) {
        if (scopeId == null) return false;
        try {
            storeClient.getById(scopeId);
            return true;
        } catch (FeignException.NotFound nf) {
            return false;
        } catch (Exception ex) {
            log.warn("StoreScopeResolver.exists({}) — commerce-service unreachable: {}",
                    scopeId, ex.getMessage());
            return false;
        }
    }

    @Override
    public Optional<ScopeRef> parentOf(UUID scopeId) {
        if (scopeId == null) return Optional.empty();
        try {
            UUID b2bUnitId = storeClient.getB2bUnitId(scopeId);
            if (b2bUnitId == null) return Optional.empty();
            return Optional.of(ScopeRef.of(ScopeType.B2B_UNIT, b2bUnitId));
        } catch (FeignException.NotFound nf) {
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("StoreScopeResolver.parentOf({}) — commerce-service unreachable: {}",
                    scopeId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Set<String> inheritableRoles() {
        return Set.of(
                ROLE_STORE_ADMIN,
                ROLE_STORE_MANAGER,
                "STORE_ADMIN",
                "STORE_MANAGER",
                Role.ROLE_BUSINESS_ADMIN,
                "BUSINESS_ADMIN"
        );
    }
}
