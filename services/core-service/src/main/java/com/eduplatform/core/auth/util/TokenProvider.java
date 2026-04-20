package com.eduplatform.core.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TokenProvider {

    @Value("${security.jwt.secret:your-super-secret-key-change-in-production-min-32-chars}")
    private String jwtSecret;

    @Value("${security.jwt.expiry:30}")
    private int jwtExpiryMinutes;

    @Value("${security.jwt.refresh-expiry:7}")
    private int refreshTokenExpiryDays;

    private SecretKey getSigningKey() {
        byte[] decodedKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Generate JWT access token (short-lived)
     */
    public String generateAccessToken(String userId, String email, String role, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        claims.put("tenantId", tenantId);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) jwtExpiryMinutes * 60 * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate refresh token (long-lived)
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) refreshTokenExpiryDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())) {
                log.warn("Token expired");
                return null;
            }

            return claims;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return null;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public int getAccessTokenExpiryMinutes() {
        return jwtExpiryMinutes;
    }
}
