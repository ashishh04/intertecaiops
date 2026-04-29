package com.juviai.user.security;

import com.juviai.user.domain.ScopeType;
import com.juviai.user.domain.User;
import com.juviai.user.repo.UserRepository;
import com.juviai.user.service.RoleAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Generic {@link ScopeType}-aware authorization bean used from SpEL via
 * {@code @PreAuthorize("@scopedSecurity.has('ROLE_STORE_ADMIN', T(com.juviai.user.domain.ScopeType).STORE, #storeId)")}
 * or the sugar annotation {@link RequiresScopedRole}.
 *
 * <p>Resolution order:</p>
 * <ol>
 *   <li><strong>Authority check</strong> — the gateway propagates scoped
 *       authorities in the form {@code ROLE_X@SCOPE:uuid} (plus the bare
 *       {@code ROLE_X}). Matching one of these is an instant accept; no DB.</li>
 *   <li><strong>DB fallback</strong> — for callers that skipped the gateway
 *       (direct tests, internal tools), we resolve the current user and ask
 *       {@link RoleAssignmentService#hasEffectiveRole} which will also walk
 *       the parent-scope chain via the {@code ScopeResolverRegistry}.</li>
 * </ol>
 */
@Component("scopedSecurity")
public class ScopedSecurity {

    private static final Logger log = LoggerFactory.getLogger(ScopedSecurity.class);

    /** Separator between role name and scope ref inside an authority string. */
    public static final String SCOPE_AUTHORITY_SEPARATOR = "@";

    private final UserRepository userRepository;
    private final RoleAssignmentService roleAssignmentService;

    public ScopedSecurity(UserRepository userRepository,
                          RoleAssignmentService roleAssignmentService) {
        this.userRepository = userRepository;
        this.roleAssignmentService = roleAssignmentService;
    }

    /**
     * Main entry used from {@code @PreAuthorize}.
     *
     * @param roleName   role to require (e.g. {@code ROLE_STORE_ADMIN} or {@code STORE_ADMIN})
     * @param scopeType  scope type ({@link ScopeType#STORE}, {@link ScopeType#B2B_UNIT}, ...)
     * @param scopeId    scope entity id; may be {@code null} for {@link ScopeType#GLOBAL}
     */
    public boolean has(String roleName, ScopeType scopeType, UUID scopeId) {
        if (roleName == null || scopeType == null) return false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // 1) Authority-based check (happy path: gateway already emitted it).
        if (authoritiesContainScopedRole(auth.getAuthorities(), roleName, scopeType, scopeId)) {
            return true;
        }

        // 2) DB fallback — resolve current user and defer to the assignment service
        //    (which also applies the parent-scope cascade).
        UUID userId = resolveCurrentUserId(auth);
        if (userId == null) return false;

        try {
            return roleAssignmentService.hasEffectiveRole(userId, roleName, scopeType, scopeId);
        } catch (Exception e) {
            log.debug("ScopedSecurity DB fallback failed for user {} role {} scope {}:{} — {}",
                    userId, roleName, scopeType, scopeId, e.getMessage());
            return false;
        }
    }

    /** Convenience overload: {@link ScopeType#GLOBAL}, any role. */
    public boolean hasGlobal(String roleName) {
        return has(roleName, ScopeType.GLOBAL, null);
    }

    /** Convenience overload — looks up by scope-type {@code String} for SpEL readability. */
    public boolean has(String roleName, String scopeTypeName, UUID scopeId) {
        try {
            return has(roleName, ScopeType.valueOf(scopeTypeName), scopeId);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /**
     * Does the authority set contain {@code ROLE@SCOPE:id} or the bare
     * {@code ROLE} (when the scope is GLOBAL)? Matches are case-sensitive on
     * role name so callers must use the canonical {@code ROLE_*} form.
     */
    private boolean authoritiesContainScopedRole(Collection<? extends GrantedAuthority> authorities,
                                                 String roleName,
                                                 ScopeType scopeType,
                                                 UUID scopeId) {
        if (authorities == null || authorities.isEmpty()) return false;

        String scoped = roleName + SCOPE_AUTHORITY_SEPARATOR + scopeType.name()
                + (scopeId == null ? "" : ":" + scopeId);

        for (GrantedAuthority ga : authorities) {
            String a = ga.getAuthority();
            if (a == null) continue;
            if (a.equals(scoped)) return true;
            if (scopeType == ScopeType.GLOBAL && a.equals(roleName)) return true;
        }
        return false;
    }

    private UUID resolveCurrentUserId(Authentication auth) {
        String email = extractEmail(auth);
        if (email == null || email.isBlank()) return null;
        return userRepository.findByEmailIgnoreCase(email)
                .map(User::getId)
                .orElse(null);
    }

    private String extractEmail(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            String email = token.getToken().getClaimAsString("email");
            if (email == null || email.isBlank()) {
                email = token.getToken().getClaimAsString("username");
            }
            if (email == null || email.isBlank()) {
                email = token.getToken().getSubject();
            }
            return email;
        }
        if (auth.getPrincipal() instanceof OAuth2AuthenticatedPrincipal p) {
            Object emailAttr = p.getAttribute("email");
            Object username = p.getAttribute("username");
            Object sub = p.getAttribute("sub");
            return emailAttr != null ? emailAttr.toString()
                    : username != null ? username.toString()
                    : sub != null ? sub.toString()
                    : null;
        }
        return auth.getName();
    }
}
