package com.juviai.user.web;

import com.juviai.user.facade.StartupIdeaFacade;
import com.juviai.user.web.dto.StartupIdeaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/startup-ideas")
@RequiredArgsConstructor
public class StartupIdeaController {

    private final StartupIdeaFacade startupIdeaFacade;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<StartupIdeaDTO>> list(
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        return ResponseEntity.ok(startupIdeaFacade.list(categoryId, pageable));
    }
}
