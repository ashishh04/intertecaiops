package com.juviai.user.converter.populator.business;

import com.juviai.user.converter.populator.MyBusinessResponsePopulator;
import com.juviai.user.domain.User;
import com.juviai.user.web.dto.MyBusinessResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMyBusinessRolesPopulator implements MyBusinessResponsePopulator {

    @Override
    public void populate(User source, MyBusinessResponseDTO target) {
        Set<String> roles = (source.getRoles() == null)
                ? Set.of()
                : source.getRoles().stream()
                .filter(Objects::nonNull)
                .map(r -> r.getName())
                .filter(x -> x != null && !x.isBlank())
                .collect(Collectors.toSet());
        if (roles.contains("ADMIN")) {
            roles.add("ROLE_ADMIN");
        }
        target.setRoles(roles.stream().sorted().toList());
    }
}
