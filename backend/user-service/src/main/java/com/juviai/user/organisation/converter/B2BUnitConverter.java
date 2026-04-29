package com.juviai.user.organisation.converter;

import com.juviai.user.converter.AbstractPopulatingConverter;
import com.juviai.user.organisation.converter.populator.B2BUnitPopulator;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.web.dto.B2BUnitDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class B2BUnitConverter extends AbstractPopulatingConverter<B2BUnit, B2BUnitDTO> {

    public B2BUnitConverter(List<B2BUnitPopulator> populators) {
        super(populators, B2BUnitDTO.class);
    }
}
