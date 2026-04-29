package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.Holiday;
import com.juviai.user.organisation.web.dto.HolidayDTO;

public class HolidayMapper {

    public static HolidayDTO toDto(Holiday holiday) {
        if (holiday == null) return null;
        HolidayDTO dto = new HolidayDTO();
        dto.setId(holiday.getId());
        dto.setDate(holiday.getDate());
        dto.setName(holiday.getName());
        return dto;
    }
}
