package com.juviai.user.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserSkillData {
    private UUID id;
    private UUID userId;
    private String name;
    private String level;
}
