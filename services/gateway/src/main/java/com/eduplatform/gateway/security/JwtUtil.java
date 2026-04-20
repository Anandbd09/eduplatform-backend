package com.eduplatform.gateway.security;

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

@Slf4j
@Component
public class JwtUtil {

    @Value("${security.jwt.secret:your-super-secret-key-change-in-production-min-32-chars}")
    private String jwtSecret;

    @Value("${security.jwt.expiry:30}")
    private int jwtExpiry;

    // Get SecretKey for verification
    private SecretKey getSigningKey() {
        byte[] decodedKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Validate JWT token and extract claims
     * @param token JWT token (without "Bearer " prefix)
     * @return Claims if valid, null if invalid
     */
    public Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())   // ✅ NEW in 0.12.x
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

    /**
     * Extract userId from claims
     */
    public String getUserId(Claims claims) {
        return claims.getSubject();  // "sub" claim contains userId
    }

    /**
     * Extract email from claims
     */
    public String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    /**
     * Extract role from claims
     */
    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Extract tenantId from claims
     */
    public String getTenantId(Claims claims) {
        return claims.get("tenantId", String.class);
    }
}