package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class HolidayDTO {
    private UUID id;
    private LocalDate date;
    private String name;
}
