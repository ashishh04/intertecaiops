package com.juviai.user.web.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import com.juviai.user.web.dto.MyBusinessResponseDTO;

public class MyBusinessResponseMapper {

    public static MyBusinessResponseDTO toDTO(User user, UUID b2bUnitId, Object businessDetails) {
        if (user == null) return null;
        Set<String> rolesSet = (user.getRoles() == null)
                ? Set.of()
                : user.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(Role::getName)
                    .filter(x -> x != null && !x.isBlank())
                    .collect(Collectors.toSet());
        if (rolesSet.contains("ADMIN")) {
            rolesSet.add("ROLE_ADMIN");
        }
        List<String> roles = rolesSet.stream().sorted().toList();

        return new MyBusinessResponseDTO(
                user.getFirstName()+' '+user.getLastName(),
                user.getId(),
                user.getEmail(),
                roles,
                b2bUnitId,
                businessDetails,
                user.isStudent()
        );
    }
}
