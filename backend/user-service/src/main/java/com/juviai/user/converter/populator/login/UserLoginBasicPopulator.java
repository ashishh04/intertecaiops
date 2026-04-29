package com.juviai.user.converter.populator.login;

import com.juviai.user.converter.populator.LoginResponsePopulator;
import com.juviai.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserLoginBasicPopulator implements LoginResponsePopulator {

    @Override
    public void populate(User source, java.util.Map<String, Object> target) {
        target.put("id", source.getId());
        target.put("username", source.getUsername());
        target.put("email", source.getEmail());
        target.put("mobile", source.getMobile());
        Set<String> roles = (source.getRoles() == null)
                ? Set.of()
                : source.getRoles().stream().map(r -> r.getName()).filter(x -> x != null && !x.isBlank()).collect(Collectors.toSet());
        if (roles.contains("ADMIN")) {
            roles.add("ROLE_ADMIN");
        }
        target.put("roles", roles.stream().sorted().toList());
        target.put("tokenVersion", source.getTokenVersion());
        target.put("active", source.isActive());
        target.put("status", source.getStatus() != null ? source.getStatus().name() : null);
    }
}
