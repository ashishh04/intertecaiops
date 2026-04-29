package com.juviai.user.organisation.web;

import com.juviai.user.organisation.domain.B2BUnitCategory;
import com.juviai.user.organisation.service.B2BUnitCategoryService;
import com.juviai.user.organisation.web.dto.B2BUnitCategoryDTO;
import com.juviai.user.organisation.web.dto.B2BUnitCategoryUpsertRequest;
import com.juviai.user.organisation.web.mapper.B2BUnitCategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/b2b/categories")
@RequiredArgsConstructor
public class B2BUnitCategoryController {

    private final B2BUnitCategoryService service;
    private static final B2BUnitCategoryMapper mapper = B2BUnitCategoryMapper.INSTANCE;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<B2BUnitCategoryDTO>> list(@PageableDefault(sort = "name", size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable).map(mapper::toDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnitCategoryDTO> get(@PathVariable("id") @NonNull UUID id) {
        B2BUnitCategory category = service.getById(id);
        return ResponseEntity.ok(mapper.toDto(category));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnitCategoryDTO> create(@RequestBody @Valid B2BUnitCategoryUpsertRequest req) {
        B2BUnitCategory created = service.create(req.getCode(), req.getName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnitCategoryDTO> update(@PathVariable("id") @NonNull UUID id,
                                                     @RequestBody @Valid B2BUnitCategoryUpsertRequest req) {
        B2BUnitCategory updated = service.update(id, req.getCode(), req.getName());
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable("id") @NonNull UUID id) {
        service.delete(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
