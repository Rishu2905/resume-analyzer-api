package com.axis.hraiportal.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry}")
    private long expiry;

    // ── Generate token on login ──────────────────────────────
    public String generateToken(String hrId, String email) {
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(hrId)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + expiry))
                .signWith(key)
                .compact();
    }

    // ── Extract hrId from token ──────────────────────────────
    public String extractHrId(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ── Extract email from token ─────────────────────────────
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    // ── Validate token ───────────────────────────────────────
    public boolean isValid(String token) {
        try {
            extractHrId(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}