package com.example.authpractice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * DEV NOTE: Refresh Token Store
 * -----------------------------
 * This table manages the "Session State" of our stateless app.
 * * Concept:
 * - Access Tokens (JWTs) are short-lived (15 mins) and not stored here.
 * - Refresh Tokens are long-lived (7 days) and ARE stored here.
 * * Security Critical:
 * - We do NOT store the raw token here. We store a HASH (SHA-256).
 * - Why? If a hacker steals our database, they only get hashes. They cannot use them
 * to impersonate users because the browser holds the 'raw' token, and we only verify matching hashes.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Allows us to create objects like: RefreshToken.builder().user(u).build();
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Relationship: Many-to-One.
     * A single User can have MULTIPLE refresh tokens.
     * Why? To support logging in on multiple devices (Laptop, Phone, Tablet) simultaneously.
     * Deleting one token (Logout) only signs them out of that specific device.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Stores the SHA-256 Hash of the token.
     * @Lob + LONGTEXT allows storage of very long strings, ensuring the hash never gets truncated.
     * (Although hashes are fixed length, this is a safe default).
     */
    @Lob
    @Column(name = "token_hash", unique = true, nullable = false, columnDefinition = "LONGTEXT")
    private String tokenHash;

    // Useful for showing the user "Last active: 2 hours ago" in a security dashboard.
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @UpdateTimestamp
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
