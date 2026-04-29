package com.juviai.user.converter.populator.startupidea;

import com.juviai.user.converter.populator.StartupIdeaPopulator;
import com.juviai.user.domain.StartupIdea;
import com.juviai.user.organisation.web.dto.B2BUnitCategoryDTO;
import com.juviai.user.web.dto.StartupIdeaDTO;
import org.springframework.stereotype.Component;

@Component
public class StartupIdeaBasicPopulator implements StartupIdeaPopulator {
    @Override
    public void populate(StartupIdea source, StartupIdeaDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setMediaUrl(source.getMediaUrl());
        target.setNumberOfLikes(source.getNumberOfLikes());
        if (source.getCategory() != null) {
            B2BUnitCategoryDTO categoryDTO = new B2BUnitCategoryDTO();
            categoryDTO.setId(source.getCategory().getId());
            categoryDTO.setName(source.getCategory().getName());
            target.setCategory(categoryDTO);
        }
    }
}
