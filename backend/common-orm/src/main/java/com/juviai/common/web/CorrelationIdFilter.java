package com.juviai.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that ensures every request has a {@code X-Correlation-ID} header
 * and makes the value available via MDC for structured logging.
 *
 * <h2>Behaviour</h2>
 * <ul>
 *   <li>If the incoming request already carries {@code X-Correlation-ID} (forwarded
 *       by the gateway or another service), that value is used as-is.</li>
 *   <li>If the header is absent, a new UUID is generated — this handles direct calls
 *       or test requests that skip the gateway.</li>
 *   <li>The resolved correlation ID is echoed back in the response as
 *       {@code X-Correlation-ID} so clients can reference it in support tickets.</li>
 *   <li>The value is stored in MDC as {@code correlationId} so it appears in every
 *       log line that uses the pattern {@code %X{correlationId}}.</li>
 * </ul>
 *
 * <h2>Logging pattern</h2>
 * Add {@code %X{correlationId}} to your Logback/Log4j2 pattern:
 * <pre>
 *   [%d] [%X{correlationId}] %-5level %logger{36} - %msg%n
 * </pre>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String MDC_KEY               = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String correlationId = request.getHeader(HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_CORRELATION_ID, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
