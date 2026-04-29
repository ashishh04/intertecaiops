package com.juviai.user.converter;

import com.juviai.user.converter.populator.AuthInfoResponsePopulator;
import com.juviai.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthInfoResponseConverter extends AbstractPopulatingConverter<User, Map<String, Object>> {

    public AuthInfoResponseConverter(List<AuthInfoResponsePopulator> populators) {
        super(populators, (Class<? extends Map<String, Object>>) (Class<?>) HashMap.class);
    }
}
