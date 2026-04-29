package com.juviai.user.facade;

import com.juviai.user.dto.CompensationDto;
import com.juviai.user.dto.CreateCompensationRequestDto;
import java.util.List;
import java.util.UUID;

public interface CompensationFacade {

    CompensationDto get(UUID id);

    List<CompensationDto> getAll();

    List<CompensationDto> listHistory(UUID employeeId);

    CompensationDto create(CreateCompensationRequestDto req);
}
