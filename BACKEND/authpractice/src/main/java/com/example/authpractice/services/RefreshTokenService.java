package com.example.authpractice.services;

import com.example.authpractice.entities.RefreshToken;
import com.example.authpractice.entities.User;
import com.example.authpractice.exceptions.TokenExpiredException;
import com.example.authpractice.repositories.RefreshTokenRepo;

import com.example.authpractice.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * DEV NOTE: Session Manager (The "Bank Vault")
 * --------------------------------------------
 * This service manages the long-lived Refresh Tokens.
 * * SECURITY ARCHITECTURE (CRITICAL):
 * 1. Hashing: We NEVER store the raw token in the database. We store SHA-256 hashes.
 * If the DB is hacked, the attacker gets useless hashes, not valid tokens.
 * 2. Rotation: Tokens are "One-Time Use". Every time a user refreshes their session,
 * we delete the old token and issue a completely new one.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepo  refreshTokenRepo;
    private final JwtService jwtService;



    public RefreshTokenService(RefreshTokenRepo refreshTokenRepo, JwtService jwtService) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtService = jwtService;

    }


    /**
     * Helper to convert "eyJ..." (Raw Token) into "a3f1..." (SHA-256 Hash).
     * This is a one-way process. You cannot turn the hash back into the token.
     */
    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }


    /**
     * Creates a brand-new session.
     * 1. Generate Raw JWT.
     * 2. Hash it.
     * 3. Save Hash to DB.
     * 4. Return Raw JWT (so the user can store it in their cookie).
     * * @Transactional(SERIALIZABLE):
     * Highest isolation level. Prevents "Race Conditions" where two threads
     * try to create/write tokens for the same user simultaneously.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String createRefreshToken(User user) {
        String rawToken = jwtService.generateRefreshToken(user.getEmail());

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepo.save(refreshToken);
        return rawToken;
    }


    /**
     * Checks if a token is valid.
     * Logic: Hash input -> Find match in DB -> Check Expiry.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RefreshToken validateAndGetToken(String rawToken) {

        //  Prevent the 500 NullPointerException
        if (rawToken == null || rawToken.isEmpty()) {
            throw new TokenExpiredException("Refresh token cookie missing");
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenExpiredException("Invalid refresh token"));

        // If found but expired, clean it up immediately.
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.deleteByTokenHash(tokenHash);
            throw new TokenExpiredException("Refresh token expired");
        }
        return refreshToken;
    }

    /**
     * THE ROTATION LOGIC (Security Best Practice).
     * When a user asks for a new Access Token, we:
     * 1. Verify their current Refresh Token.
     * 2. DELETE IT immediately (it's now "burned").
     * 3. Issue a brand new Refresh Token.
     * * Why?
     * If a hacker steals a refresh token, they can only use it ONCE.
     * If the real user uses it first, the hacker's token becomes invalid.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String rotateRefreshToken(String oldRawToken, User user) {
        validateAndGetToken(oldRawToken); // Ensure old one exists and is valid
        String tokenHash= hashToken(oldRawToken);
        refreshTokenRepo.deleteByTokenHash(tokenHash); // delete the old one
        return createRefreshToken(user); // issue a new one
    }

    // Revokes a specific session (Logout).
    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        refreshTokenRepo.deleteByTokenHash(tokenHash);
    }

    // "Nuclear Option": Logs the user out of ALL devices (Phone, Laptop, etc).
    //useful if admin wants to revoke all the sessions of that particular user
    @Transactional
    public void revokeAllTokensOfUser(User user) {
        refreshTokenRepo.findByUser(user).forEach(refreshTokenRepo::delete);
    }

    // Janitor task to clean up dead rows.
    @Transactional
    public void cleanExpiredTokens() {
       refreshTokenRepo.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
