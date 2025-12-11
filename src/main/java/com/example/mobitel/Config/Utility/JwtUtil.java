

package com.example.mobitel.Config.Utility;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final String SECRET = "SecretKeyForJWTMustBe32CharLong!";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // Generate Token
//    public String generateToken(String username, String user_role) {
//        try {
//            return Jwts.builder()
//                    .setSubject(username)
//                    .claim("role", user_role)
//                    .setIssuedAt(new Date())
//                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))  // 1 hour expiry
//                    .signWith(secretKey, SignatureAlgorithm.HS256)
//                    .compact();
//        } catch (Exception e) {
//            log.error("Error generating Token: " + e.getMessage(), e);
//            return null;
//        }
//    }

    public String generateToken(String username, String role, String systemUserId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("systemUserId", systemUserId)   // Include user_id in token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }



    // Extract username from token
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

        } catch (Exception e) {
            log.error("Error extracting username: " + e.getMessage(), e);
            return null;
        }
    }




    // Validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
