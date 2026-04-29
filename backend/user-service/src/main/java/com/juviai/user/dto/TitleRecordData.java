package com.juviai.user.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TitleRecordData {
    private UUID id;
    private UUID userId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}
