package com.juviai.user.security;

import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import com.juviai.user.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component("b2bSecurity")
public class B2BSecurity {

    private final UserRepository userRepository;

    public B2BSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasBusinessOrHrAdmin(UUID b2bUnitId) {
        if (b2bUnitId == null) return false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = null;
        if (auth instanceof JwtAuthenticationToken token) {
            email = token.getToken().getClaimAsString("email");
            if (email == null || email.isBlank()) {
                email = token.getToken().getClaimAsString("username");
            }
            if (email == null || email.isBlank()) {
                email = token.getToken().getSubject();
            }
        } else if (auth != null && auth.getPrincipal() instanceof OAuth2AuthenticatedPrincipal p) {
            Object emailAttr = p.getAttribute("email");
            Object username = p.getAttribute("username");
            Object sub = p.getAttribute("sub");
            email = (emailAttr != null) ? emailAttr.toString()
                    : (username != null) ? username.toString()
                    : (sub != null) ? sub.toString()
                    : null;
        }

        if (email == null || email.isBlank()) return false;
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) return false;
        Set<Role> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) return false;
        return roles.stream()
                .filter(r -> r != null && r.getName() != null)
                .anyMatch(r ->
                    Objects.equals(b2bUnitId, r.getB2bUnitId()) &&
                    isBusinessOrHrAdminRole(r.getName())
                );
    }

    private boolean isBusinessOrHrAdminRole(String roleName) {
        if (roleName == null) {
            return false;
        }
        String n = roleName.trim();
        if (n.isEmpty()) {
            return false;
        }
        return "BUSINESS_ADMIN".equalsIgnoreCase(n)
                || "ROLE_BUSINESS_ADMIN".equalsIgnoreCase(n)
                || "HR_ADMIN".equalsIgnoreCase(n)
                || "ROLE_HR_ADMIN".equalsIgnoreCase(n);
    }
}
