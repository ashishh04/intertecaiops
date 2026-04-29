package com.juviai.user.facade.impl;

import com.juviai.user.converter.DesignationConverter;
import com.juviai.user.domain.Designation;
import com.juviai.user.dto.DesignationData;
import com.juviai.user.dto.DesignationDTO;
import com.juviai.user.dto.DesignationRequestDTO;
import com.juviai.user.facade.DesignationFacade;
import com.juviai.user.service.DesignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DesignationFacadeImpl implements DesignationFacade {

    private final DesignationService designationService;
    private final DesignationConverter designationConverter;

    @Override
    public List<DesignationDTO> listDesignations(UUID b2bUnitId) {
        return designationService.getDesignations(b2bUnitId);
    }

    @Override
    public List<DesignationDTO> allDesignations(UUID b2bUnitId) {
        return designationService.getAllDesignations(b2bUnitId);
    }

    @Override
    public DesignationData createDesignation(DesignationRequestDTO request) {
        Designation d = designationService.createDesignation(request);
        return designationConverter.convert(d);
    }

    @Override
    public DesignationData updateDesignation(UUID id, DesignationRequestDTO request) {
        Designation d = designationService.updateDesignation(id, request);
        return designationConverter.convert(d);
    }

    @Override
    public void deleteDesignation(UUID id) {
        designationService.deleteDesignation(id);
    }
}
