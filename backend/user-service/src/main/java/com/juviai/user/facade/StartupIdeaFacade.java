package com.juviai.user.facade;

import com.juviai.user.web.dto.StartupIdeaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface StartupIdeaFacade {
    Page<StartupIdeaDTO> list(UUID categoryId, Pageable pageable);
}
