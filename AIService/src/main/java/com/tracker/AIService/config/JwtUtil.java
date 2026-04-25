package com.tracker.AIService.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public Long extractUserId(String token) {
        try {
            token = token.replace("Bearer ", "");

            return Long.valueOf(
                    Jwts.parser()
                            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getSubject()
            );
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired");
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token");
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid userId in token");
        }
    }
}
