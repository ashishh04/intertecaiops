package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateHolidayCalendarRequest {
    private String cityCode;
    private String name;
}
