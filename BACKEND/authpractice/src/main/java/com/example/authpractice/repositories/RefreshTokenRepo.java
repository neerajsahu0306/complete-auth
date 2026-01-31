package com.example.authpractice.repositories;

import com.example.authpractice.entities.RefreshToken;
import com.example.authpractice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DEV NOTE: Refresh Token Access
 * ------------------------------
 * Manages the long-lived session tokens.
 * * CRITICAL SECURITY NOTE:
 * We never query by the "raw" token string because we don't store it.
 * We always search by 'tokenHash' (SHA-256).
 */
@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, String> {

    // Looks up a token by its secure hash.
   Optional<RefreshToken> findByTokenHash(String token);

    /**
     * Finds ALL active sessions for a specific user.
     * Used when we want to implement "Logout from all devices".
     */
   List<RefreshToken> findByUser(User user);

    // Revokes a specific session (Logout).
   @Transactional
   @Modifying
   void deleteByTokenHash(String tokenHash);

    // Auto-cleanup for tokens that have passed their 7-day lifespan.
    @Transactional
    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime now);
}
