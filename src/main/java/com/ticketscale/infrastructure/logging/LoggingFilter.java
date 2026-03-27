package com.ticketscale.infrastructure.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para adicionar correlation ID em todas as requisições.
 * O correlation ID é usado para rastreamento distribuído de logs.
 */
@Component
public class LoggingFilter implements Filter {

    private static final String CORRELATION_ID = "correlationId";
    private static final String TIMESTAMP = "timestamp";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            MDC.put(CORRELATION_ID, UUID.randomUUID().toString());
            MDC.put(TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
