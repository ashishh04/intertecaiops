package com.juviai.user.organisation.facade;

import com.juviai.user.organisation.web.dto.DepartmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DepartmentFacade {
    Page<DepartmentDTO> getAllDepartments(UUID b2bUnitId, Pageable pageable);
    Page<DepartmentDTO> searchDepartments(UUID b2bUnitId, String query, PageRequest pr);
    Page<DepartmentDTO> getActiveDepartments(UUID b2bUnitId, Pageable pageable);
    List<DepartmentDTO> listActiveDepartments(UUID b2bUnitId);
    DepartmentDTO getDepartmentById(UUID id);
    DepartmentDTO createDepartment(DepartmentDTO dto);
    DepartmentDTO updateDepartment(UUID id, DepartmentDTO dto);
    void deleteDepartment(UUID id);
}
