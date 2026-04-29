package com.juviai.user.organisation.web;

import com.juviai.user.organisation.domain.City;
import com.juviai.user.organisation.importer.ExcelCityImportParser;
import com.juviai.user.organisation.service.CityService;
import com.juviai.user.organisation.web.dto.CityDTO;
import com.juviai.user.organisation.web.dto.CityExcelImportResponse;
import com.juviai.user.organisation.web.mapper.CityMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;
    private final ExcelCityImportParser excelCityImportParser;

    private static final CityMapper cityMapper = CityMapper.INSTANCE;

    @GetMapping
    public ResponseEntity<Page<CityDTO>> findAll(@RequestParam(value = "q", required = false) String q,
                                                 Pageable pageable) {
        Page<CityDTO> page = cityService.getAll(q, pageable).map(cityMapper::toDto);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CityDTO>> findActiveOrderedByName() {
        List<CityDTO> list = cityService.getActiveCitiesOrderedByName().stream().map(cityMapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-state/{stateCode}")
    public ResponseEntity<List<CityDTO>> findByStateCode(@PathVariable("stateCode") @NonNull String stateCode,
                                                         @RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(cityService.findByStateCode(stateCode, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityDTO> getById(@PathVariable @NonNull UUID id) {
        City city = cityService.getById(id);
        return ResponseEntity.ok(cityMapper.toDto(city));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CityDTO> getByCode(@PathVariable @NonNull String code) {
        City city = cityService.getByCode(code);
        return ResponseEntity.ok(cityMapper.toDto(city));
    }

    @PostMapping
    public ResponseEntity<CityDTO> create(@RequestBody CityDTO dto) {
        City created = cityService.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(cityMapper.toDto(created));
    }

    @PostMapping(value = "/admin/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CityExcelImportResponse importExcel(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        List<ExcelCityImportParser.ImportRowError> errors = new ArrayList<>();
        List<ExcelCityImportParser.CityImportRow> rows;
        try {
            rows = excelCityImportParser.parse(file.getInputStream(), errors);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage());
        }

        int created = 0;
        for (ExcelCityImportParser.CityImportRow r : rows) {
            try {
                CityDTO dto = new CityDTO();
                dto.setName(r.getName());
                dto.setCode(r.getCode());
                dto.setActive(r.isActive());
                dto.setStateCode(r.getStateCode());
                cityService.create(dto);
                created++;
            } catch (Exception e) {
                errors.add(new ExcelCityImportParser.ImportRowError(r.getRowNumber(), r.getCode(), e.getMessage()));
            }
        }

        CityExcelImportResponse resp = new CityExcelImportResponse();
        resp.setTotalRows(rows.size());
        resp.setCreatedCount(created);
        resp.setFailedCount(errors.size());
        resp.setErrors(errors);
        return resp;
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityDTO> update(@PathVariable @NonNull UUID id, @RequestBody CityDTO dto) {
        City updated = cityService.update(id, dto);
        return ResponseEntity.ok(cityMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull UUID id) {
        cityService.delete(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
