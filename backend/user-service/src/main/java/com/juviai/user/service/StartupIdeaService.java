package com.juviai.user.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.StartupIdea;
import com.juviai.user.repo.StartupIdeaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class StartupIdeaService {

    private final StartupIdeaRepository repository;

    public StartupIdeaService(StartupIdeaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<StartupIdea> list(UUID categoryId, Pageable pageable) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        return repository.list(tenantId, categoryId, pageable);
    }
}
