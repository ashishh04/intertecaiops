package com.juviai.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        // For unauthenticated flows (e.g., public signup), use a fixed auditor
        return () -> Optional.of("system");
    }
}
