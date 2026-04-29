package com.juviai.common.security;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration that registers {@link GatewaySigningRequestInterceptor} as a
 * <strong>global</strong> Feign {@link RequestInterceptor}.
 *
 * <p>Spring Cloud Feign picks up every {@code RequestInterceptor} bean in the
 * application context and applies it to ALL {@code @FeignClient} interfaces.
 * By declaring this in {@code common-orm} (which every service scans), we avoid
 * copy-pasting the interceptor registration into every service's configuration.
 *
 * <p>The {@code @ConditionalOnClass} guard ensures this config is only activated
 * when {@code feign.RequestInterceptor} is actually on the classpath — services that
 * don't use Feign simply skip this bean.
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class GatewayFeignAutoConfig {

    @Bean
    public RequestInterceptor gatewaySigningRequestInterceptor(GatewaySecurityProperties props) {
        return new GatewaySigningRequestInterceptor(props);
    }
}
