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
 * DEV NOTE: Temporary OTP Storage
 * -------------------------------
 * This table acts as a "Holding Area" for user verification.
 * It is used for both Signups and Password Resets.
 * * Cleanup:
 * Rows in this table are short-lived. Our 'ScheduleTasks' job automatically
 * deletes rows where 'expiresAt' has passed to keep the table small.
 */
@Entity
@Table(name = "otp_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Unique Constraint on Email.
     * This means a user can only have ONE active OTP at a time.
     * If they request a new one, we overwrite the existing row or update it.
     * This prevents spamming the DB with 50 OTPs for the same person.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;


    /**
     * Brute Force Protection.
     * We track how many times the user entered a WRONG code.
     * If this hits a limit (e.g., 3), we can invalidate the OTP or block the request.
     */
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // Usually set to creation_time + 5
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
