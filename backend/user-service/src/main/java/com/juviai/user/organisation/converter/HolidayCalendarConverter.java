package com.juviai.user.organisation.converter;

import com.juviai.user.converter.AbstractPopulatingConverter;
import com.juviai.user.organisation.converter.populator.HolidayCalendarPopulator;
import com.juviai.user.organisation.domain.HolidayCalendar;
import com.juviai.user.organisation.web.dto.HolidayCalendarDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HolidayCalendarConverter extends AbstractPopulatingConverter<HolidayCalendar, HolidayCalendarDTO> {

    public HolidayCalendarConverter(List<HolidayCalendarPopulator> populators) {
        super(populators, HolidayCalendarDTO.class);
    }
}
