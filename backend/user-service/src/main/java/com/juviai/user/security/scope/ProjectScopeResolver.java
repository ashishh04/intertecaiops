package com.juviai.user.security.scope;

import com.juviai.user.client.ProjectClient;
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
 * Scope resolver for {@link ScopeType#PROJECT}. Delegates to project-service
 * via {@link ProjectClient} for existence and parent-B2B-unit lookup.
 *
 * <p>If project-service is unreachable (network error, timeout, etc.), the
 * resolver degrades gracefully: {@link #exists(UUID)} returns {@code false}
 * (so a role assignment is not created against an unvalidated scope) and
 * {@link #parentOf(UUID)} returns empty (so cascade checks stop cleanly
 * without crashing the caller).</p>
 *
 * <p>Direct role-assignment lookups do not go through this resolver, so a
 * project-service outage never breaks authorization of users who already have
 * a direct grant at the project.</p>
 */
@Component
public class ProjectScopeResolver implements ScopeResolver {

    private static final Logger log = LoggerFactory.getLogger(ProjectScopeResolver.class);

    private final ProjectClient projectClient;

    public ProjectScopeResolver(ProjectClient projectClient) {
        this.projectClient = projectClient;
    }

    @Override
    public ScopeType type() {
        return ScopeType.PROJECT;
    }

    @Override
    public boolean exists(UUID scopeId) {
        if (scopeId == null) return false;
        try {
            ProjectClient.ProjectLite p = projectClient.getProject(scopeId);
            return p != null && p.getId() != null;
        } catch (FeignException.NotFound nf) {
            return false;
        } catch (Exception ex) {
            log.warn("ProjectScopeResolver.exists({}) — project-service unreachable: {}",
                    scopeId, ex.getMessage());
            return false;
        }
    }

    @Override
    public Optional<ScopeRef> parentOf(UUID scopeId) {
        if (scopeId == null) return Optional.empty();
        try {
            ProjectClient.ProjectLite p = projectClient.getProject(scopeId);
            if (p == null || p.getB2bUnitId() == null) return Optional.empty();
            return Optional.of(ScopeRef.of(ScopeType.B2B_UNIT, p.getB2bUnitId()));
        } catch (FeignException.NotFound nf) {
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("ProjectScopeResolver.parentOf({}) — project-service unreachable: {}",
                    scopeId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Set<String> inheritableRoles() {
        return Set.of(
                Role.ROLE_PROJECT_MANAGER,
                "PROJECT_MANAGER"
        );
    }
}
