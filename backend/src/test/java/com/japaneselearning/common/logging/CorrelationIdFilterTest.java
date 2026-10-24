package com.japaneselearning.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link CorrelationIdFilter}.
 * Verifies correlation ID generation, propagation, header handling, and MDC cleanup.
 */
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void cleanupMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate UUID correlation ID when no header is provided")
    void shouldGenerateCorrelationIdWhenNoHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Capture MDC value during filter chain execution
        doAnswer(invocation -> {
            String correlationId = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID);
            assertThat(correlationId).isNotNull().isNotBlank();
            // UUID format check
            assertThat(correlationId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        // Verify response header is set
        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isNotNull();

        // Verify MDC is cleaned up after filter
        assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isNull();
    }

    @Test
    @DisplayName("Should use X-Correlation-ID from request header when provided")
    void shouldUseProvidedCorrelationId() throws ServletException, IOException {
        String providedId = "test-correlation-id-123";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, providedId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isEqualTo(providedId);
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(providedId);
    }

    @Test
    @DisplayName("Should fallback to X-Request-ID when X-Correlation-ID is not set")
    void shouldFallbackToRequestIdHeader() throws ServletException, IOException {
        String requestId = "request-id-456";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, requestId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isEqualTo(requestId);
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);
    }

    @Test
    @DisplayName("Should populate request method, URI, and client IP in MDC")
    void shouldPopulateRequestMetadataInMdc() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_METHOD)).isEqualTo("POST");
            assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI)).isEqualTo("/api/auth/login");
            assertThat(MDC.get(CorrelationIdFilter.MDC_CLIENT_IP)).isEqualTo("192.168.1.100");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        // Verify all MDC entries are cleaned up
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_METHOD)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_CLIENT_IP)).isNull();
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void shouldExtractClientIpFromXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1, 172.16.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            // Should take the first IP (original client)
            assertThat(MDC.get(CorrelationIdFilter.MDC_CLIENT_IP)).isEqualTo("10.0.0.1");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);
    }

    @Test
    @DisplayName("Should clean up MDC even when filter chain throws exception")
    void shouldCleanUpMdcOnException() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doThrow(new ServletException("Test error")).when(chain).doFilter(request, response);

        try {
            filter.doFilterInternal(request, response, chain);
        } catch (ServletException e) {
            // Expected
        }

        // MDC must be cleaned up even after exception
        assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_METHOD)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_REQUEST_URI)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.MDC_CLIENT_IP)).isNull();
    }
}
