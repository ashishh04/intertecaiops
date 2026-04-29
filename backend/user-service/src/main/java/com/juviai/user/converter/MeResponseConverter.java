package com.juviai.user.converter;

import com.juviai.user.domain.User;
import com.juviai.user.converter.populator.MeResponsePopulator;
import com.juviai.user.web.dto.MeResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeResponseConverter extends AbstractPopulatingConverter<User, MeResponseDTO> {

    public MeResponseConverter(List<MeResponsePopulator> populators) {
        super(populators, MeResponseDTO.class);
    }
}
