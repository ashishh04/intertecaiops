package com.juviai.user.converter.populator.me;

import com.juviai.user.converter.populator.MeResponsePopulator;
import com.juviai.user.domain.User;
import com.juviai.user.web.dto.MeResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMeBasicPopulator implements MeResponsePopulator {

    @Override
    public void populate(User source, MeResponseDTO target) {
        target.setId(source.getId());
        target.setUsername(source.getUsername());
        target.setEmail(source.getEmail());
        target.setMobile(source.getMobile());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
    }
}
