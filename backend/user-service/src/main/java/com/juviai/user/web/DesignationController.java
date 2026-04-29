package com.juviai.user.web;

import com.juviai.user.dto.DesignationData;
import com.juviai.user.dto.DesignationDTO;
import com.juviai.user.dto.DesignationRequestDTO;
import com.juviai.user.facade.DesignationFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/designations")
public class DesignationController {

    private final DesignationFacade designationFacade;

    public DesignationController(DesignationFacade designationFacade) {
        this.designationFacade = designationFacade;
    }

    @GetMapping("/list/{b2bUnitId}")
    public List<DesignationDTO> listDesignations(@PathVariable UUID b2bUnitId) {
        return designationFacade.listDesignations(b2bUnitId);
    }

    @GetMapping("/all/{b2bUnitId}")
    public List<DesignationDTO> allDesignations(@PathVariable UUID b2bUnitId) {
        return designationFacade.allDesignations(b2bUnitId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DesignationData createDesignation(@RequestBody @Valid DesignationRequestDTO request) {
        return designationFacade.createDesignation(request);
    }

    @PutMapping("/{id}")
    public DesignationData updateDesignation(@PathVariable @NonNull UUID id,
                                            @RequestBody @Valid DesignationRequestDTO request) {
        return designationFacade.updateDesignation(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDesignation(@PathVariable @NonNull UUID id) {
        designationFacade.deleteDesignation(id);
    }
}
