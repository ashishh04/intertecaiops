package com.juviai.user.web.mapper;

import com.juviai.user.domain.StartupIdea;
import com.juviai.user.organisation.web.mapper.B2BUnitCategoryMapper;
import com.juviai.user.web.dto.StartupIdeaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {B2BUnitCategoryMapper.class})
public interface StartupIdeaMapper {

    StartupIdeaMapper INSTANCE = Mappers.getMapper(StartupIdeaMapper.class);

    StartupIdeaDTO toDto(StartupIdea idea);
}
