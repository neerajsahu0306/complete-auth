package com.example.authpractice.repositories;

import com.example.authpractice.entities.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DEV NOTE: OTP Storage Access
 * ----------------------------
 * Handles the temporary storage of 6-digit codes.
 */
@Repository
public interface OtpVerificationRepo extends JpaRepository<OtpVerification, String> {

    // Finds the active OTP for a user.
    Optional<OtpVerification> findByEmail(String email);

    // Deletes expired OTPs (older than 5 mins) to keep the table clean.
    @Transactional
    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * RATE LIMITING QUERY.
     * Counts how many OTPs this email has requested in the last X hours.
     * * Why?
     * To prevent abuse. If this returns > 5, we can block the user from
     * requesting another OTP for a while.
     */
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.email = :email AND o.createdAt > :oneHourAgo")
    Long countRecentOTPsByEmail(@Param("email") String email, @Param("oneHourAgo") LocalDateTime oneHourAgo);


}
