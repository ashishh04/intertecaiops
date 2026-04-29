package com.juviai.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DesignationData {
    private UUID id;
    private String name;
    private UUID b2bUnitId;
    private String bandName;
    private UUID bandId;
}
