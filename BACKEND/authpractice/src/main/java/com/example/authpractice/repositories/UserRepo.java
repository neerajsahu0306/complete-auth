package com.example.authpractice.repositories;

import com.example.authpractice.entities.AuthProvider;
import com.example.authpractice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;



/**
 * DEV NOTE: User Data Access
 * --------------------------
 * Handles database operations for the 'user' table.
 * Extends JpaRepository to give us free methods like save(), findById(), etc.
 */
@Repository
public interface UserRepo extends JpaRepository<User, String> {

    // Finds a user by their unique email (used during Login & Signup checks).
   Optional<User> findByEmail(String email);

    /**
     * Finds a user specifically for OAuth logins.
     * * Why both fields?
     * Because 'oauthId' is only unique within that provider.
     * Theoretically, the ID "12345" could exist in both Google AND Facebook.
     * So we search by Pair: (Provider=GOOGLE, ID=12345).
     */
    Optional<User> findByOauthIdAndAuthProvider(String oauthId, AuthProvider authProvider);

    // Quick check to see if an email is already taken before creating a new user.
   Boolean existsByEmail(String email);


    /**
     * CLEANUP QUERY (The Janitor).
     * Deletes users who signed up but never verified their email within the cutoff time (24h).
     * * @Transactional + @Modifying: Required for custom DELETE/UPDATE queries in Spring Data.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.isVerified = false AND u.createdAt < :cutoff")
    void deleteUnverifiedUsers(@Param("cutoff") LocalDateTime cutoff);
}
