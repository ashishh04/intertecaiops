package com.juviai.user.facade;

import com.juviai.user.dto.DesignationData;
import com.juviai.user.dto.DesignationDTO;
import com.juviai.user.dto.DesignationRequestDTO;

import java.util.List;
import java.util.UUID;

public interface DesignationFacade {
    List<DesignationDTO> listDesignations(UUID b2bUnitId);
    List<DesignationDTO> allDesignations(UUID b2bUnitId);
    DesignationData createDesignation(DesignationRequestDTO request);
    DesignationData updateDesignation(UUID id, DesignationRequestDTO request);
    void deleteDesignation(UUID id);
}
