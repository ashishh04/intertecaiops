package com.juviai.user.organisation.web;

import com.juviai.user.organisation.domain.State;
import com.juviai.user.organisation.importer.ExcelStateImportParser;
import com.juviai.user.organisation.service.StateService;
import com.juviai.user.organisation.web.dto.StateDTO;
import com.juviai.user.organisation.web.dto.StateExcelImportResponse;
import com.juviai.user.organisation.web.mapper.StateMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
public class StateController {

    private final StateService stateService;
    private final ExcelStateImportParser excelStateImportParser;

    private static final StateMapper stateMapper = StateMapper.INSTANCE;

    @GetMapping
    public ResponseEntity<List<StateDTO>> findAll() {
        List<StateDTO> list = stateService.getAll().stream().map(stateMapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StateDTO> getById(@PathVariable @NonNull UUID id) {
        State s = stateService.getById(id);
        return ResponseEntity.ok(stateMapper.toDto(s));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<StateDTO> getByCode(@PathVariable @NonNull String code) {
        State s = stateService.getByCode(code);
        return ResponseEntity.ok(stateMapper.toDto(s));
    }

    @PostMapping
    public ResponseEntity<StateDTO> create(@RequestBody StateDTO dto) {
        State created = stateService.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(stateMapper.toDto(created));
    }

    @PostMapping(value = "/admin/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public StateExcelImportResponse importExcel(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        List<ExcelStateImportParser.ImportRowError> errors = new ArrayList<>();
        List<ExcelStateImportParser.StateImportRow> rows;
        try {
            rows = excelStateImportParser.parse(file.getInputStream(), errors);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage());
        }

        int created = 0;
        for (ExcelStateImportParser.StateImportRow r : rows) {
            try {
                StateDTO dto = new StateDTO();
                dto.setName(r.getName());
                dto.setCode(r.getCode());
                dto.setActive(r.isActive());
                dto.setCountryCode(r.getCountryCode());
                stateService.create(dto);
                created++;
            } catch (Exception e) {
                errors.add(new ExcelStateImportParser.ImportRowError(r.getRowNumber(), r.getCode(), e.getMessage()));
            }
        }

        StateExcelImportResponse resp = new StateExcelImportResponse();
        resp.setTotalRows(rows.size());
        resp.setCreatedCount(created);
        resp.setFailedCount(errors.size());
        resp.setErrors(errors);
        return resp;
    }

    @PutMapping("/{id}")
    public ResponseEntity<StateDTO> update(@PathVariable @NonNull UUID id, @RequestBody StateDTO dto) {
        State updated = stateService.update(id, dto);
        return ResponseEntity.ok(stateMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull UUID id) {
        stateService.delete(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
