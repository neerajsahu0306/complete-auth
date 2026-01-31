package com.example.authpractice.security;


import com.example.authpractice.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * DEV NOTE: The Token Factory
 * ---------------------------
 * This service handles all the cryptography for JSON Web Tokens (JWT).
 */
@Service
public class JwtService {

    // The secret used to sign tokens. If anyone gets this, they can forge admin tokens.
    // In production, this should be a very long, random string stored in Environment Variables.
    // Configured in application.properties.
    private final String SECRET_KEY;

    // Configured in application.properties.
    private final long ACCESS_TOKEN_EXPIRATION; // Short-lived (e.g., 15 mins)

    // Configured in application.properties.
    private final long REFRESH_TOKEN_EXPIRATION; // Long-lived (e.g., 7 days)

    // Injection via Constructor ensures these values are loaded before the service starts
    public JwtService(
          @Value("${jwt.secret}")  String secretKey,
          @Value("${jwt.access-token-expiration}")   long accessTokenExpiration,
          @Value("${jwt.refresh-token-expiration}")   long refreshTokenExpiration) {
        SECRET_KEY = secretKey;
        ACCESS_TOKEN_EXPIRATION = accessTokenExpiration;
        REFRESH_TOKEN_EXPIRATION = refreshTokenExpiration;
    }


    /**
     * Converts our String secret into a standard Cryptographic Key object.
     * We use HMAC-SHA algorithms (HS256).
     */
    private Key getSigningKey() {
       byte[] keyBytes = SECRET_KEY.getBytes();
       return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * The core method that actually builds the token string.
     * Structure: Header . Payload (Claims) . Signature
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims) // The data (payload)
                .setSubject(subject) // Usually the Email or userId
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // The digital seal
                .compact();
    }

    /**
     * Generates an Access Token with "Extra Claims".
     * * OPTIMIZATION:
     * We embed 'userId' and 'role' directly into the token.
     * This allows the JwtAuthenticationFilter to set up the user context
     * WITHOUT needing to query the database. Big performance boost!
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        return createToken(claims, user.getEmail(), ACCESS_TOKEN_EXPIRATION);
    }

    // Refresh tokens usually don't need extra claims, just the identity (email).
    public String generateRefreshToken(String email) {
        return createToken(new HashMap<>(), email, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * The "Verification" Step.
     * This parses the token string. If the signature doesn't match our Secret Key,
     * or if the token is expired, this line throws an Exception.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- Helper Methods to get data out of the token ---

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Checks if the token belongs to the user claiming it,
     * AND if it is still within its expiration window.
     */
    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
