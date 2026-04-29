package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.State;
import com.juviai.user.organisation.web.dto.StateDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StateMapper {
    StateMapper INSTANCE = Mappers.getMapper(StateMapper.class);

    @Mapping(target = "countryCode", expression = "java(state.getCountry() != null ? state.getCountry().getCode() : null)")
    StateDTO toDto(State state);

    @Mapping(target = "country", ignore = true)
    State toEntity(StateDTO dto);
}
