package com.juviai.user.converter;

import com.juviai.user.domain.User;
import com.juviai.user.converter.populator.MyBusinessResponsePopulator;
import com.juviai.user.web.dto.MyBusinessResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyBusinessResponseConverter extends AbstractPopulatingConverter<User, MyBusinessResponseDTO> {

    public MyBusinessResponseConverter(List<MyBusinessResponsePopulator> populators) {
        super(populators, MyBusinessResponseDTO.class);
    }
}
