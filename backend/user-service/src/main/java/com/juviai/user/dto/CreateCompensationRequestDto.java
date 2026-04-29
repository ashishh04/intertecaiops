package com.juviai.user.dto;

import com.juviai.user.domain.CompensationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompensationRequestDto {
    private UUID employeeId;
    private CompensationType type;
    private BigDecimal amount;
    private LocalDate effectiveStartDate;
    private boolean active;
}
