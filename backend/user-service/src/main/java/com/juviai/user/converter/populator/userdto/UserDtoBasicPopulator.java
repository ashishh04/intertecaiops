package com.juviai.user.converter.populator.userdto;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.converter.populator.UserDtoPopulator;
import com.juviai.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoBasicPopulator implements UserDtoPopulator {

    @Override
    public void populate(User source, UserDTO target) {
        target.setId(source.getId());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setEmail(source.getEmail());
        target.setStudent(source.isStudent());
    }
}
