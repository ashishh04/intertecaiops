package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.B2BUnitCategory;
import com.juviai.user.organisation.web.dto.B2BUnitCategoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface B2BUnitCategoryMapper {

    B2BUnitCategoryMapper INSTANCE = Mappers.getMapper(B2BUnitCategoryMapper.class);

    B2BUnitCategoryDTO toDto(B2BUnitCategory category);
}
