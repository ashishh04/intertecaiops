package com.juviai.user.organisation.web;

import com.juviai.user.organisation.facade.DepartmentFacade;
import com.juviai.user.organisation.web.dto.DepartmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentFacade departmentFacade;

    @GetMapping
    public Page<DepartmentDTO> getAllDepartments(
            @RequestParam @NonNull UUID b2bUnitId,
            @PageableDefault(sort = "name", size = 10) Pageable pageable) {
        return departmentFacade.getAllDepartments(b2bUnitId, pageable);
    }

    @GetMapping("/search")
    public Page<DepartmentDTO> searchDepartments(
            @RequestParam @NonNull UUID b2bUnitId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return departmentFacade.searchDepartments(b2bUnitId, query, PageRequest.of(page, size, sort));
    }

    @GetMapping("/active")
    public Page<DepartmentDTO> getActiveDepartments(
            @RequestParam UUID b2bUnitId,
            @PageableDefault(sort = "name", size = 10) Pageable pageable) {
        return departmentFacade.getActiveDepartments(b2bUnitId, pageable);
    }

    @GetMapping("/active/list")
    public List<DepartmentDTO> listActiveDepartments(
            @RequestParam @NonNull UUID b2bUnitId) {
        return departmentFacade.listActiveDepartments(b2bUnitId);
    }

    @GetMapping("/{id}")
    public DepartmentDTO getDepartmentById(@PathVariable @NonNull UUID id) {
        return departmentFacade.getDepartmentById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentDTO createDepartment(@RequestBody DepartmentDTO departmentDTO) {
        return departmentFacade.createDepartment(departmentDTO);
    }

    @PutMapping("/{id}")
    public DepartmentDTO updateDepartment(@PathVariable @NonNull UUID id,
                                          @RequestBody DepartmentDTO dto) {
        return departmentFacade.updateDepartment(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(@PathVariable @NonNull UUID id) {
        departmentFacade.deleteDepartment(id);
    }
}
