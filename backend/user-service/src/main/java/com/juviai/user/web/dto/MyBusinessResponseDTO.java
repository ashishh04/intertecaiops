package com.juviai.user.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyBusinessResponseDTO {
    private String name;
    private UUID id;
    private String email;
    private List<String> roles;
    private UUID b2bUnitId;
    private Object business;
    private boolean student;
}
