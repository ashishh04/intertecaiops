package com.juviai.common.config;

import com.juviai.common.tenant.TenantAwareAuditor;
import com.juviai.common.tenant.TenantFilter;
import com.juviai.common.tenant.TenantTokenValidationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class CommonOrmConfig {

    @Bean
    @ConditionalOnMissingBean(name = "auditorAware")
    public TenantAwareAuditor auditorAware() {
        return new TenantAwareAuditor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "tenantFilter")
    public OncePerRequestFilter tenantFilter() {
        // base domain fallback; override via property in services
        return new TenantFilter("juviai.io");
    }

    @Bean
    @ConditionalOnMissingBean(name = "tenantTokenValidationFilter")
    public OncePerRequestFilter tenantTokenValidationFilter() {
        return new TenantTokenValidationFilter();
    }
}
