package com.juviai.user.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
}
