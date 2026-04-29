package com.juviai.user.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EducationData {
    private UUID id;
    private UUID userId;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
}
