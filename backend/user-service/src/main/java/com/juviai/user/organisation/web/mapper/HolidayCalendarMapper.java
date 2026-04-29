package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.City;
import com.juviai.user.organisation.domain.HolidayCalendar;
import com.juviai.user.organisation.web.dto.HolidayCalendarDTO;

public class HolidayCalendarMapper {

    public static HolidayCalendarDTO toDto(HolidayCalendar calendar) {
        if (calendar == null) return null;
        HolidayCalendarDTO dto = new HolidayCalendarDTO();
        dto.setId(calendar.getId());
        dto.setName(calendar.getName());
        City city = calendar.getCity();
        if (city != null) {
            dto.setCityCode(city.getCode());
            dto.setCityName(city.getName());
        }
        return dto;
    }
}
