package com.juviai.user.web.mapper;

import com.juviai.user.domain.User;
import com.juviai.user.web.dto.MeResponseDTO;

public class MeResponseMapper {

    public static MeResponseDTO toDTO(User user) {
        if (user == null) return null;
        return new MeResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getMobile(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
