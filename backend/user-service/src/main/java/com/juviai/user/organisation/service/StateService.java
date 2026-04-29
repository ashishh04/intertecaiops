package com.juviai.user.organisation.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.organisation.domain.Country;
import com.juviai.user.organisation.domain.State;
import com.juviai.user.organisation.repo.CountryRepository;
import com.juviai.user.organisation.repo.StateRepository;
import com.juviai.user.organisation.web.dto.StateDTO;
import com.juviai.user.organisation.web.mapper.StateMapper;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StateService {

    private static final Logger log = LoggerFactory.getLogger(StateService.class);

    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;

    private static final StateMapper stateMapper = StateMapper.INSTANCE;



    @Transactional(readOnly = true)
    public State getById(@NonNull UUID id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("State not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public State getByCode(@NonNull String code) {
        return stateRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("State not found with id: " + code));
    }


    @Transactional(readOnly = true)
    public List<State> getAll() {
        return stateRepository.findAll();
    }

    @SuppressWarnings("null")
    @Transactional
    public State create(StateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("State payload is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }

        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");

        State s = stateMapper.toEntity(dto);
        s.setName(dto.getName().trim());
        s.setCode(dto.getCode().trim());
        s.setTenantId(tenantId);

        if (dto.getCountryCode() != null) {
            Country c = countryRepository.findByCode(dto.getCountryCode())
                    .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + dto.getCountryCode()));
            s.setCountry(c);
        }

        return stateRepository.save(s);
    }

    @SuppressWarnings("null")
    @Transactional
    public State update(@NonNull UUID id, StateDTO dto) {
        State existing = getById(id);

        if (dto != null) {
            if (dto.getName() != null && !dto.getName().isBlank() && !dto.getName().trim().equals(existing.getName())) {
                existing.setName(dto.getName().trim());
            }
            if (dto.getCode() != null && !dto.getCode().isBlank() && !dto.getCode().trim().equals(existing.getCode())) {
                existing.setCode(dto.getCode().trim());
            }
            existing.setActive(dto.isActive());

            if (dto.getCountryCode() != null) {
                Country c = countryRepository.findByCode(dto.getCountryCode())
                        .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + dto.getCountryCode()));
                existing.setCountry(c);
            } else {
                existing.setCountry(null);
            }
        }
        return stateRepository.save(existing);
    }

    @Transactional
    public void delete(@NonNull UUID id) {
        State existing = getById(id);
        stateRepository.delete(existing);
        log.info("State deleted id={}, name={}", existing.getId(), existing.getName());
    }
}
