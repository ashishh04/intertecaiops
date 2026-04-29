package com.juviai.user.facade;

import com.juviai.user.web.dto.EmployeeBandRequestDTO;
import com.juviai.user.web.dto.EmployeeBandResponseDTO;
import java.util.List;
import java.util.UUID;

public interface EmployeeBandFacade {
    EmployeeBandResponseDTO createBand(EmployeeBandRequestDTO request);
    List<EmployeeBandResponseDTO> getBandsByB2bUnit(UUID b2bUnitId);
    EmployeeBandResponseDTO getBand(UUID id);
    EmployeeBandResponseDTO updateBand(UUID id, EmployeeBandRequestDTO request);
    void deleteBand(UUID id);
}
