package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.City;
import com.juviai.user.organisation.web.dto.CityDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CityMapper {
    CityMapper INSTANCE = Mappers.getMapper(CityMapper.class);

    @Mapping(target = "stateCode", expression = "java(city.getState() != null ? city.getState().getCode() : null)")
    CityDTO toDto(City city);

    @Mapping(target = "state", ignore = true)
    City toEntity(CityDTO dto);
}
