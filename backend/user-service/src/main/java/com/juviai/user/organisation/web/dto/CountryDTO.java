package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CountryDTO {
    private UUID id;
    private String name;
    private String code;
    private boolean active;
}
