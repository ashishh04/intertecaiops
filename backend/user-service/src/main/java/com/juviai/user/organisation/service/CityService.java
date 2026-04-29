package com.juviai.user.organisation.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.organisation.domain.City;
import com.juviai.user.organisation.domain.State;
import com.juviai.user.organisation.repo.CityRepository;
import com.juviai.user.organisation.repo.StateRepository;
import com.juviai.user.organisation.web.dto.CityDTO;
import com.juviai.user.organisation.web.mapper.CityMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final StateRepository stateRepository;

    private static final CityMapper cityMapper = CityMapper.INSTANCE;

    @Transactional(readOnly = true)
    public List<CityDTO> findByStateCode(String stateCode, String q) {
        if (stateCode == null || stateCode.isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return cityRepository.findByStateCodeAndQuery(stateCode.trim(), query)
                .stream()
                .map(cityMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<City> getAll() {
        return cityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<City> getAll(String q, Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        String query = (q == null || q.isBlank()) ? null : q.trim();
        if (query == null) {
            return cityRepository.findByTenantId(tenantId, pageable);
        }
        return cityRepository.searchByTenantId(tenantId, query, pageable);
    }

    @Transactional(readOnly = true)
    public List<City> getActiveCitiesOrderedByName() {
        return cityRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public City getById(@NonNull UUID id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public City getByCode(@NonNull String code) {
        return cityRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("City not found with code: " + code));
    }

    @SuppressWarnings("null")
    @Transactional
    public City create(CityDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("City payload is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (dto.getStateCode() == null || dto.getStateCode().isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }

        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");

        City city = cityMapper.toEntity(dto);
        city.setName(dto.getName().trim());
        city.setCode(dto.getCode().trim());
        city.setTenantId(tenantId);

        State state = stateRepository.findByCode(dto.getStateCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("State not found with code: " + dto.getStateCode()));
        city.setState(state);

        return cityRepository.save(city);
    }

    @SuppressWarnings("null")
    @Transactional
    public City update(@NonNull UUID id, CityDTO dto) {
        City existing = getById(id);

        if (dto != null) {
            if (dto.getName() != null && !dto.getName().isBlank()) {
                existing.setName(dto.getName().trim());
            }
            if (dto.getCode() != null && !dto.getCode().isBlank()) {
                existing.setCode(dto.getCode().trim());
            }
            existing.setActive(dto.isActive());

            if (dto.getStateCode() != null && !dto.getStateCode().isBlank()) {
                State state = stateRepository.findByCode(dto.getStateCode().trim())
                        .orElseThrow(() -> new IllegalArgumentException("State not found with code: " + dto.getStateCode()));
                existing.setState(state);
            }
        }

        return cityRepository.save(existing);
    }

    @Transactional
    public void delete(@NonNull UUID id) {
        City existing = getById(id);
        cityRepository.delete(existing);
    }
}
