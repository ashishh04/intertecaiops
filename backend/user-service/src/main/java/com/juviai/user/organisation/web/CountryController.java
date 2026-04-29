package com.juviai.user.organisation.web;

import com.juviai.user.organisation.domain.Country;
import com.juviai.user.organisation.service.CountryService;
import com.juviai.user.organisation.web.dto.CountryDTO;
import com.juviai.user.organisation.web.mapper.CountryMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    private static final CountryMapper countryMapper = CountryMapper.INSTANCE;

    @GetMapping
    public List<Country> findAll() {
        return countryService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryDTO> getById(@PathVariable @NonNull UUID id) {
        Country c = countryService.getById(id);
        return ResponseEntity.ok(countryMapper.toDto(c));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CountryDTO> findByCode(@PathVariable @NonNull String code) {
        Country country = countryService.getByCode(code);
        return ResponseEntity.ok(countryMapper.toDto(country));
    }

    @PostMapping
    public ResponseEntity<CountryDTO> create(@RequestBody CountryDTO dto) {
        Country created = countryService.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(countryMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryDTO> update(@PathVariable @NonNull UUID id, @RequestBody CountryDTO dto) {
        Country updated = countryService.update(id, dto);
        return ResponseEntity.ok(countryMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull UUID id) {
        countryService.delete(id);
        return ResponseEntity.<Void>noContent().build();
    }
}
