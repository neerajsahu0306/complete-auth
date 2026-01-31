package com.example.authpractice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;


/**
 * DEV NOTE: The User Entity
 * -------------------------
 * This class maps to the 'user' table in MySQL.
 * It is designed to handle a "Hybrid Authentication" system.
 * * * Scenarios supported:
 * 1. Local User: Has 'email', 'password', and authProvider = LOCAL.
 * 2. Google User: Has 'email', 'oauthId', authProvider = GOOGLE, and usually NULL password.
 */

@Table(name = "user")
@Entity
@Data // Generates Getters, Setters, ToString, EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Primary Key.
     * We use UUID (String) instead of Auto-Increment (1, 2, 3).
     * Why? It's more secure (URL guessing is harder) and safer for database scaling later.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * Stores the BCrypt hash of the password.
     * NOTE: This can be NULL if the user signed up via Google (OAuth2).
     */
    @Column(name = "password")
    private String password;


    /**
     * @Enumerated(EnumType.STRING)
     * Tells Hibernate to store the Enum as its name ("USER", "ADMIN") in the database
     * instead of its index number (0, 1).
     * This keeps the data readable and prevents corruption if we change the Enum order later.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role; // Enum: USER, ADMIN

    /**
     * Critical for Local Auth.
     * - False by default.
     * - Becomes True only after they enter, and we verify the OTP.
     * - We block login if this is false (except for OAuth users who are auto-verified).
     */
    @Column(name = "isverified" )
    private boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    /**
     * Distinguishes how this user registered.
     * Default is LOCAL. If they login via Google, we switch/set this to GOOGLE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;


    /**
     * Stores the unique ID provided by the external provider (e.g., Google 'sub' claim).
     * Used to link a returning Google user to their existing account.
     */
    @Column(name = "oauth_id")
    private String oauthId;


}
