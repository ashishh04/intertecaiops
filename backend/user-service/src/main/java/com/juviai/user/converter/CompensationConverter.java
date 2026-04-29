package com.juviai.user.converter;

import com.juviai.user.converter.populator.CompensationPopulator;
import com.juviai.user.domain.Compensation;
import com.juviai.user.dto.CompensationDto;
import org.springframework.stereotype.Component;

@Component
public class CompensationConverter extends AbstractPopulatingConverter<Compensation, CompensationDto> {

    public CompensationConverter(java.util.List<CompensationPopulator> populators) {
        super(populators, CompensationDto.class);
    }
}
