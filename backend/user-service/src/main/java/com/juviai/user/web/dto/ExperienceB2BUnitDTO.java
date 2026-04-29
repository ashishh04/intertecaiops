package com.juviai.user.web.dto;

import com.juviai.user.organisation.domain.B2BUnitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExperienceB2BUnitDTO {
    private UUID id;
    private String name;
    private B2BUnitType type;
}
