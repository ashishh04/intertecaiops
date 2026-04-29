package com.juviai.user.organisation.converter.populator.holidaycalendar;

import com.juviai.user.organisation.converter.populator.HolidayCalendarPopulator;
import com.juviai.user.organisation.domain.City;
import com.juviai.user.organisation.domain.HolidayCalendar;
import com.juviai.user.organisation.web.dto.HolidayCalendarDTO;
import org.springframework.stereotype.Component;

@Component
public class HolidayCalendarBasicPopulator implements HolidayCalendarPopulator {

    @Override
    public void populate(HolidayCalendar source, HolidayCalendarDTO target) {
        if (source == null || target == null) return;
        target.setId(source.getId());
        target.setName(source.getName());
        if (source.getB2bUnit() != null) {
            target.setB2bUnitId(source.getB2bUnit().getId());
        }
        City city = source.getCity();
        if (city != null) {
            target.setCityCode(city.getCode());
            target.setCityName(city.getName());
        }
    }
}
