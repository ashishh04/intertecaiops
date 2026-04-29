package com.juviai.user.organisation.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.organisation.domain.B2BUnitCategory;
import com.juviai.user.organisation.repo.B2BUnitCategoryRepository;
import com.juviai.user.organisation.repo.B2BUnitRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class B2BUnitCategoryService {

    private static final Logger log = LoggerFactory.getLogger(B2BUnitCategoryService.class);

    private final B2BUnitCategoryRepository repository;
    private final B2BUnitRepository b2bUnitRepository;

    @Transactional(readOnly = true)
    public Page<B2BUnitCategory> list(Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        return repository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public B2BUnitCategory getById(@NonNull UUID id) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnitCategory not found with id: " + id));
    }

    @Transactional
    public B2BUnitCategory create(@NonNull String code, @NonNull String name) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");

        String c = code.trim();
        String n = name.trim();

        if (repository.existsByCodeIgnoreCaseAndTenantId(c, tenantId)) {
            throw new IllegalArgumentException("B2BUnitCategory with code already exists");
        }
        if (repository.existsByNameIgnoreCaseAndTenantId(n, tenantId)) {
            throw new IllegalArgumentException("B2BUnitCategory with name already exists");
        }

        B2BUnitCategory category = new B2BUnitCategory();
        category.setTenantId(tenantId);
        category.setCode(c);
        category.setName(n);

        B2BUnitCategory saved = repository.save(category);
        log.info("B2BUnitCategory created id={}, code={}, tenantId={}", saved.getId(), saved.getCode(), tenantId);
        return saved;
    }

    @Transactional
    public B2BUnitCategory update(@NonNull UUID id, @NonNull String code, @NonNull String name) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        B2BUnitCategory existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnitCategory not found with id: " + id));

        String c = code.trim();
        String n = name.trim();

        if (!existing.getCode().equalsIgnoreCase(c) && repository.existsByCodeIgnoreCaseAndTenantId(c, tenantId)) {
            throw new IllegalArgumentException("B2BUnitCategory with code already exists");
        }
        if (!existing.getName().equalsIgnoreCase(n) && repository.existsByNameIgnoreCaseAndTenantId(n, tenantId)) {
            throw new IllegalArgumentException("B2BUnitCategory with name already exists");
        }

        existing.setCode(c);
        existing.setName(n);

        B2BUnitCategory saved = repository.save(existing);
        log.info("B2BUnitCategory updated id={}, code={}, tenantId={}", saved.getId(), saved.getCode(), tenantId);
        return saved;
    }

    @Transactional
    public void delete(@NonNull UUID id) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        B2BUnitCategory category = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("B2BUnitCategory not found with id: " + id));

        if (b2bUnitRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("B2BUnitCategory is in use and cannot be deleted");
        }

        repository.delete(category);
        log.info("B2BUnitCategory deleted id={}, tenantId={}", id, tenantId);
    }
}
