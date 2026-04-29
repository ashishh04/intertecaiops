package com.juviai.user.converter;

import com.juviai.user.converter.populator.ExperienceDetailsResponsePopulator;
import com.juviai.user.domain.Experience;
import com.juviai.user.web.dto.ExperienceDetailsResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExperienceDetailsResponseConverter extends AbstractPopulatingConverter<Experience, ExperienceDetailsResponseDTO> {

    public ExperienceDetailsResponseConverter(List<ExperienceDetailsResponsePopulator> populators) {
        super(populators, ExperienceDetailsResponseDTO.class);
    }
}
