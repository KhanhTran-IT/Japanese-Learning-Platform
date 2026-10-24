package com.japaneselearning.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that manages correlation IDs for distributed tracing.
 * <p>
 * Placed as the first filter in the chain ({@code Ordered.HIGHEST_PRECEDENCE + 10})
 * so every subsequent filter, controller, and service call has the correlation ID
 * available in the MDC for structured logging.
 *
 * <h3>Behavior:</h3>
 * <ol>
 *     <li>Checks for incoming {@code X-Correlation-ID} or {@code X-Request-ID} header</li>
 *     <li>If absent, generates a new UUID v4</li>
 *     <li>Stores correlation ID in SLF4J MDC under key {@code correlationId}</li>
 *     <li>Also stores request method, URI, and client IP in MDC for structured logs</li>
 *     <li>Sets {@code X-Correlation-ID} on the response for client-side correlation</li>
 *     <li>Cleans up MDC in {@code finally} block to prevent thread-pool leaks</li>
 * </ol>
 *
 * <h3>Why this matters:</h3>
 * <ul>
 *     <li>Every log line can be traced to a specific HTTP request</li>
 *     <li>Client-supplied correlation IDs propagate through the system</li>
 *     <li>Response header allows client to reference the same ID for support tickets</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_METHOD = "requestMethod";
    public static final String MDC_REQUEST_URI = "requestUri";
    public static final String MDC_CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extract or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = request.getHeader(REQUEST_ID_HEADER);
            }
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // 2. Populate MDC for all downstream logging
            MDC.put(MDC_CORRELATION_ID, correlationId);
            MDC.put(MDC_REQUEST_METHOD, request.getMethod());
            MDC.put(MDC_REQUEST_URI, request.getRequestURI());
            MDC.put(MDC_CLIENT_IP, getClientIp(request));

            // 3. Set correlation ID on response header for client tracing
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // 4. Continue filter chain
            filterChain.doFilter(request, response);
        } finally {
            // 5. Clean up MDC to prevent thread-pool contamination
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_METHOD);
            MDC.remove(MDC_REQUEST_URI);
            MDC.remove(MDC_CLIENT_IP);
        }
    }

    /**
     * Extracts client IP from proxy headers (X-Forwarded-For) or falls back to remote address.
     * Takes only the first IP in X-Forwarded-For chain (the original client).
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For can be "client, proxy1, proxy2" — take the first
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
