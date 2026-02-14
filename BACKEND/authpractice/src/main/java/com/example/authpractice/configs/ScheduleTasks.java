package com.example.authpractice.configs;

import com.example.authpractice.services.OTPService;
import com.example.authpractice.services.RefreshTokenService;
import com.example.authpractice.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * DEV NOTE: Background Cleanup Jobs (The Janitor)
 * -----------------------------------------------
 * This component handles automated maintenance tasks.
 * Since we are using @EnableScheduling in our main app, Spring will detect these
 * @Scheduled methods and run them on a loop.
 * * WHY IS THIS CRITICAL?
 * 1. Security: We don't want expired OTPs valid forever (even if logic blocks them, they clutter DB).
 * 2. Performance: Storing millions of old Refresh Tokens slows down login queries.
 * 3. UX: Deleting unverified users allows people to retry signup if they messed up the first time.
 */

@Component
@Slf4j
public class ScheduleTasks {
    private final OTPService otpService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public ScheduleTasks(OTPService otpService, RefreshTokenService refreshTokenService, UserService userService) {
        this.otpService = otpService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    /**
     * HOURLY CLEANUP (fixedRate = 3600000 ms = 1 Hour)
     * * Tasks:
     * 1. Delete expired OTPs (usually valid for only 5-10 mins).
     * 2. Delete expired Refresh Tokens (usually valid for 7 days).
     * * Why every hour?
     * These tables grow fast. OTPs are generated frequently. Cleaning them hourly keeps the table light.
     */

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredData() {
        log.info(" Starting cleanup of expired OTPs and Refresh Tokens");

        try {
            otpService.cleanExpiredOTPs();
            refreshTokenService.cleanExpiredTokens();
            log.info(" Database cleanup finished successfully");
        } catch (Exception e) {
            log.error("Janitor Job Error: Failed to clean expired data. Check DB connection. {}", e.getMessage(), e);
        }
    }


    /**
     * DAILY CLEANUP (fixedRate = 86400000 ms = 24 Hours)
     * * Task:
     * - Delete users who signed up but NEVER verified their email after 24 hours.
     * * Why?
     * Prevents "Email Squatting". If someone registers "example@gmail.com" but abandons it,
     * the real "example" can't sign up. This frees up the email for the real owner.
     */

    @Scheduled(fixedRate = 86400000)
    public void cleanupUsers() {
        log.info(" Starting daily removal of unverified/stale users");
        try {
            userService.cleanUnverifiedUsers();
            log.info(" Daily user cleanup completed");
        } catch (Exception e) {
            log.error(" Daily user cleanup failed! Reason: {}", e.getMessage(), e);
        }
    }
}
