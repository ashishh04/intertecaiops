package com.juviai.user.organisation.facade.impl;

import com.juviai.user.organisation.converter.DepartmentConverter;
import com.juviai.user.organisation.facade.DepartmentFacade;
import com.juviai.user.organisation.service.DepartmentService;
import com.juviai.user.organisation.web.dto.DepartmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DepartmentFacadeImpl implements DepartmentFacade {

    private final DepartmentService departmentService;
    private final DepartmentConverter departmentConverter;

    @Override
    public Page<DepartmentDTO> getAllDepartments(UUID b2bUnitId, Pageable pageable) {
        return departmentService.getAllDepartments(b2bUnitId, pageable)
                .map(departmentConverter::convert);
    }

    @Override
    public Page<DepartmentDTO> searchDepartments(UUID b2bUnitId, String query, PageRequest pr) {
        return departmentService.searchDepartments(b2bUnitId, query, pr)
                .map(departmentConverter::convert);
    }

    @Override
    public Page<DepartmentDTO> getActiveDepartments(UUID b2bUnitId, Pageable pageable) {
        return departmentService.getActiveDepartments(b2bUnitId, pageable)
                .map(departmentConverter::convert);
    }

    @Override
    public List<DepartmentDTO> listActiveDepartments(UUID b2bUnitId) {
        return departmentConverter.convertAll(departmentService.listActiveDepartments(b2bUnitId));
    }

    @Override
    public DepartmentDTO getDepartmentById(UUID id) {
        return departmentConverter.convert(departmentService.getDepartmentById(id));
    }

    @Override
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        return departmentConverter.convert(departmentService.createDepartment(dto));
    }

    @Override
    public DepartmentDTO updateDepartment(UUID id, DepartmentDTO dto) {
        dto.setId(id);
        return departmentConverter.convert(departmentService.updateDepartment(id, dto));
    }

    @Override
    public void deleteDepartment(UUID id) {
        departmentService.deleteDepartment(id);
    }
}
