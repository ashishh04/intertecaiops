package com.juviai.user.organisation.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.organisation.domain.Country;
import com.juviai.user.organisation.repo.CountryRepository;
import com.juviai.user.organisation.web.dto.CountryDTO;
import com.juviai.user.organisation.web.mapper.CountryMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    private static final CountryMapper countryMapper = CountryMapper.INSTANCE;

    @Transactional(readOnly = true)
    public List<Country> getAll() {
        return countryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Country getById(@NonNull UUID id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Country getByCode(@NonNull String code) {
        return countryRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + code));
    }

    @SuppressWarnings("null")
    @Transactional
    public Country create(CountryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Country payload is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }

        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");

        Country c = countryMapper.toEntity(dto);
        c.setName(dto.getName().trim());
        c.setCode(dto.getCode().trim());
        c.setTenantId(tenantId);

        return countryRepository.save(c);
    }

    @SuppressWarnings("null")
    @Transactional
    public Country update(@NonNull UUID id, CountryDTO dto) {
        Country existing = getById(id);

        if (dto != null) {
            if (dto.getName() != null && !dto.getName().isBlank()) {
                existing.setName(dto.getName().trim());
            }
            if (dto.getCode() != null && !dto.getCode().isBlank()) {
                existing.setCode(dto.getCode().trim());
            }
            existing.setActive(dto.isActive());
        }

        return countryRepository.save(existing);
    }

    @Transactional
    public void delete(@NonNull UUID id) {
        Country existing = getById(id);
        countryRepository.delete(existing);
    }
}
