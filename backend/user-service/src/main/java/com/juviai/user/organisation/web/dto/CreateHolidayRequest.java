package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateHolidayRequest {
    private LocalDate date;
    private String name;
}
