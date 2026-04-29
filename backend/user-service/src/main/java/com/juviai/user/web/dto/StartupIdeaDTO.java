package com.juviai.user.web.dto;

import com.juviai.user.organisation.web.dto.B2BUnitCategoryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StartupIdeaDTO {
    private UUID id;
    private String name;
    private String description;
    private String mediaUrl;
    private B2BUnitCategoryDTO category;
    private long numberOfLikes;
}
