package com.japaneselearning.module_auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class CookieUtil {

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${jwt.expiration.refresh:604800000}")
    private long refreshExpirationMs;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @PostConstruct
    public void validateConfig() {
        if ("None".equalsIgnoreCase(sameSite) && !secure) {
            throw new IllegalStateException("Invalid Cookie Configuration: SameSite=None requires secure=true. Please update your environment variables.");
        }
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        long maxAgeSeconds = refreshExpirationMs / 1000;
        
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie createClearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(0) // 0 means delete immediately
                .build();
    }
}
