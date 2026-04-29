package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HolidayCalendarDTO {
    private UUID id;
    private String name;
    private String cityCode;
    private String cityName;
    private UUID b2bUnitId;
}
