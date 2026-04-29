package com.juviai.user.web;

import com.juviai.user.facade.EmployeeBandFacade;
import com.juviai.user.web.dto.EmployeeBandRequestDTO;
import com.juviai.user.web.dto.EmployeeBandResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employeebands")
@RequiredArgsConstructor
public class EmployeeBandController {

    private final EmployeeBandFacade employeeBandFacade;

    @PostMapping("/create")
    public ResponseEntity<EmployeeBandResponseDTO> createBand(
            @Valid @RequestBody EmployeeBandRequestDTO bandDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeBandFacade.createBand(bandDTO));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeBandResponseDTO>> getBandsByB2bUnit(
            @RequestParam UUID b2bUnitId) {
        return ResponseEntity.ok(employeeBandFacade.getBandsByB2bUnit(b2bUnitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeBandResponseDTO> getBand(
            @PathVariable @NonNull UUID id) {
        return ResponseEntity.ok(employeeBandFacade.getBand(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeBandResponseDTO> updateBand(
            @PathVariable @NonNull UUID id,
            @RequestBody EmployeeBandRequestDTO request) {
        return ResponseEntity.ok(employeeBandFacade.updateBand(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBand(@PathVariable @NonNull UUID id) {
        employeeBandFacade.deleteBand(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
