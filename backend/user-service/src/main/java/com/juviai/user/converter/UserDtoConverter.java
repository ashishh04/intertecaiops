package com.juviai.user.converter;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.converter.populator.UserDtoPopulator;
import com.juviai.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDtoConverter extends AbstractPopulatingConverter<User, UserDTO> {

    public UserDtoConverter(List<UserDtoPopulator> populators) {
        super(populators, UserDTO.class);
    }
}
