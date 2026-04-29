package com.juviai.user.organisation.web.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateDTO {
    private UUID id;
    private String name;
    private String code;
    private boolean active;
    private String countryCode;
}
