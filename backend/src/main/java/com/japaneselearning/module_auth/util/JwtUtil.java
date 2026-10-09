package com.japaneselearning.module_auth.util;

import com.japaneselearning.module_user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret.access}")
    private String accessSecret;

    @Value("${jwt.secret.refresh}")
    private String refreshSecret;

    @Value("${jwt.expiration.access}")
    private long accessExpiration;

    @Value("${jwt.expiration.refresh}")
    private long refreshExpiration;

    // ================= Access Token Methods =================

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return buildToken(claims, user.getEmail(), accessExpiration, getAccessSignInKey());
    }

    public boolean isAccessTokenValid(String token, String userEmail) {
        final String extractedEmail = extractAccessEmail(token);
        return (extractedEmail.equals(userEmail)) && !isAccessTokenExpired(token);
    }

    public String extractAccessEmail(String token) {
        return extractAccessClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> extractRoles(String token) {
        return extractAccessClaim(token, claims -> claims.get("roles", java.util.List.class));
    }

    private boolean isAccessTokenExpired(String token) {
        return extractAccessExpiration(token).before(new Date());
    }

    private Date extractAccessExpiration(String token) {
        return extractAccessClaim(token, Claims::getExpiration);
    }

    private <T> T extractAccessClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllAccessClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllAccessClaims(String token) {
        return Jwts.parser()
                .verifyWith(getAccessSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getAccessSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= Refresh Token Methods =================

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        return buildToken(claims, user.getEmail(), refreshExpiration, getRefreshSignInKey());
    }

    public boolean isRefreshTokenValid(String token, String userEmail) {
        final String extractedEmail = extractRefreshEmail(token);
        return (extractedEmail.equals(userEmail)) && !isRefreshTokenExpired(token);
    }

    public String extractRefreshEmail(String token) {
        return extractRefreshClaim(token, Claims::getSubject);
    }

    private boolean isRefreshTokenExpired(String token) {
        return extractRefreshExpiration(token).before(new Date());
    }

    private Date extractRefreshExpiration(String token) {
        return extractRefreshClaim(token, Claims::getExpiration);
    }

    private <T> T extractRefreshClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllRefreshClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getRefreshSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= Common =================

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration, SecretKey key) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
}
