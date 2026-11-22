package com.japaneselearning.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Filter to authenticate internal monitoring requests (such as Prometheus scraper)
 * using a shared secret passed via 'X-Monitoring-Secret' header or 'Authorization: Bearer <secret>'.
 * <p>
 * This filter only applies to Actuator endpoints (/actuator/**) and grants 'ROLE_MONITORING'
 * authority upon successful verification of the monitoring secret.
 */
@Component
@Slf4j
public class MonitoringAuthenticationFilter extends OncePerRequestFilter {

    public static final String MONITORING_HEADER = "X-Monitoring-Secret";
    public static final String MONITORING_ROLE = "ROLE_MONITORING";
    public static final String MONITORING_PRINCIPAL = "internal-monitoring";

    @Value("${app.monitoring.secret:}")
    private String monitoringSecret;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // Only process internal monitoring credentials for actuator paths
        if (uri != null && uri.startsWith("/actuator") && isConfigured()) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String candidateToken = extractCandidateToken(request);
                if (candidateToken != null && matchesSecret(candidateToken)) {
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(MONITORING_ROLE));
                    UserDetails principal = new User(MONITORING_PRINCIPAL, "", authorities);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                    log.debug("Internal monitoring access authenticated for URI: {}", uri);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isConfigured() {
        return monitoringSecret != null && !monitoringSecret.isBlank();
    }

    private String extractCandidateToken(HttpServletRequest request) {
        String secretHeader = request.getHeader(MONITORING_HEADER);
        if (secretHeader != null && !secretHeader.isBlank()) {
            return secretHeader.trim();
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private boolean matchesSecret(String candidate) {
        byte[] expected = monitoringSecret.trim().getBytes(StandardCharsets.UTF_8);
        byte[] actual = candidate.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
