package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.Country;
import com.juviai.user.organisation.web.dto.CountryDTO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CountryMapper {
    CountryMapper INSTANCE = Mappers.getMapper(CountryMapper.class);

    CountryDTO toDto(Country country);

    Country toEntity(CountryDTO dto);
}
