package com.juviai.user.converter;

import com.juviai.user.converter.populator.StartupIdeaPopulator;
import com.juviai.user.domain.StartupIdea;
import com.juviai.user.web.dto.StartupIdeaDTO;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StartupIdeaConverter extends AbstractPopulatingConverter<StartupIdea, StartupIdeaDTO> {
    public StartupIdeaConverter(List<StartupIdeaPopulator> populators) {
        super(populators, StartupIdeaDTO.class);
    }
}
