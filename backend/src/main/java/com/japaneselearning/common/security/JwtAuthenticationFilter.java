package com.japaneselearning.common.security;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter — builds Authentication from token claims.
 * <p>
 * Supported with bounded-time / immediate token invalidation via {@link TokenRevocationService}:
 * User status changes (LOCKED, DELETED), password change, and token version changes
 * are validated against an L1/L2 cache (without hitting the primary database on every request).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRevocationService tokenRevocationService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            userEmail = jwtUtil.extractAccessEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isAccessTokenValid(jwt, userEmail)) {
                    Long userId = jwtUtil.extractUserId(jwt);
                    Integer tokenVersion = jwtUtil.extractTokenVersion(jwt);

                    // Check token validity against revocation cache (version, account lock, deletion)
                    if (!tokenRevocationService.isTokenActive(userId, tokenVersion, userEmail)) {
                        log.warn("Access token rejected by TokenRevocationService for user ID: {}, email: {}", userId, userEmail);
                        writeErrorResponse(response, ErrorCode.ACCESS_TOKEN_REVOKED);
                        return;
                    }

                    List<GrantedAuthority> authorities = jwtUtil.extractRoles(jwt).stream()
                            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    UserDetails userDetails = new User(userEmail, "", authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            log.error("JWT is expired: {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (SignatureException e) {
            log.error("JWT signature is invalid: {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
        } catch (AppException e) {
            log.error("AppException during JWT auth: {}", e.getMessage());
            writeErrorResponse(response, e.getErrorCode());
        } catch (Exception e) {
            log.error("Exception during JWT auth", e);
            writeErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(errorCode.getHttpStatus().value());
            response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            com.japaneselearning.common.response.ApiResponse<?> apiResponse =
                    com.japaneselearning.common.response.ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
            objectMapper.writeValue(response.getWriter(), apiResponse);
            response.flushBuffer();
        }
    }
}
