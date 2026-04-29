package com.juviai.user.organisation.converter;

import com.juviai.user.converter.AbstractPopulatingConverter;
import com.juviai.user.organisation.converter.populator.HolidayPopulator;
import com.juviai.user.organisation.domain.Holiday;
import com.juviai.user.organisation.web.dto.HolidayDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HolidayConverter extends AbstractPopulatingConverter<Holiday, HolidayDTO> {

    public HolidayConverter(List<HolidayPopulator> populators) {
        super(populators, HolidayDTO.class);
    }
}
