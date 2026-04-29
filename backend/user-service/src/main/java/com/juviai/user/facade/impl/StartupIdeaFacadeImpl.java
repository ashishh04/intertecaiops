package com.juviai.user.facade.impl;

import com.juviai.user.converter.StartupIdeaConverter;
import com.juviai.user.facade.StartupIdeaFacade;
import com.juviai.user.service.StartupIdeaService;
import com.juviai.user.web.dto.StartupIdeaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartupIdeaFacadeImpl implements StartupIdeaFacade {

    private final StartupIdeaService startupIdeaService;
    private final StartupIdeaConverter startupIdeaConverter;

    @Override
    public Page<StartupIdeaDTO> list(UUID categoryId, Pageable pageable) {
        return startupIdeaService.list(categoryId, pageable)
                .map(startupIdeaConverter::convert);
    }
}
