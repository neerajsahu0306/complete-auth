package com.example.authpractice.services;

import com.example.authpractice.entities.OtpVerification;
import com.example.authpractice.exceptions.TooManyRequestsException;
import com.example.authpractice.repositories.OtpVerificationRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;


/**
 * DEV NOTE: OTP Manager
 * ---------------------
 * This service handles the lifecycle of the 6-digit codes used for verification.
 * * SECURITY FEATURES BUILT-IN:
 * 1. Rate Limiting: Prevents a user from requesting 100 emails in an hour (Spam protection).
 * 2. Brute Force Protection: Prevents a user from guessing the code by trying 1000 times.
 * 3. Expiration: Codes die after X minutes.
 */
@Service
@Slf4j
public class OTPService {

    private final OtpVerificationRepo otpVerificationRepo;
    private final EmailService emailService;

    // Configurable limits from application.properties
    private final int otpExpiryMinutes; // e.g., 5 minutes
    private final int maxAttempts; // e.g., 3 failed tries allowed
    private final int maxRequestsPerHour; // e.g., 5 emails per hour max


    public OTPService(OtpVerificationRepo otpVerificationRepo, EmailService emailService, @Value("${otp.expiry-minutes}") int otpExpiryMinutes, @Value("${otp.max-attempts}") int maxAttempts, @Value("${otp.max-requests-per-hour}") int maxRequestsPerHour) {
        this.otpVerificationRepo = otpVerificationRepo;
        this.emailService = emailService;
        this.otpExpiryMinutes = otpExpiryMinutes;
        this.maxAttempts = maxAttempts;
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    // Generates a random number between 100000 and 999999
    public String generateOTP() {
        Random random = new Random();
        int otp =  random.nextInt(100000, 999999);
        return String.valueOf(otp);
    }

    /**
     * The Main Sender Method.
     * 1. Check Rate Limit -> 2. Delete Old OTP -> 3. Create New -> 4. Send Email
     */
    @Transactional
    public void createAndSendOTP(String mail) {
        log.info("OTP Request: Initiating for email: {}", mail);
        // SECURITY CHECK: Rate Limiting
        // We count how many OTPs were generated for this email in the last hour.
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentOTPCount = otpVerificationRepo.countRecentOTPsByEmail(mail, oneHourAgo);
        if (recentOTPCount >= maxRequestsPerHour) {
            log.warn("OTP Security: Rate limit hit for {}. Count: {}", mail, recentOTPCount);
            throw new TooManyRequestsException("Too many OTP requests");
        }

        // Cleanup: If an old OTP exists (that wasn't used), remove it.
        // We only want ONE active OTP per email at a time.
        otpVerificationRepo.findByEmail(mail).ifPresent(otp -> {
            log.debug("OTP Cleanup: Removing existing active OTP for {}", mail);
            otpVerificationRepo.delete(otp);
        });

        // Creation Logic
        String otpCode = generateOTP();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(mail);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setExpiresAt(expiryTime);
        otpVerification.setAttemptCount(0); // Reset attempts
        otpVerificationRepo.save(otpVerification);
        log.info("OTP Success: Code generated and stored for {}. Expires at: {}", mail, expiryTime);

        // Async call to send the actual email
        emailService.sendOTPEmail(mail, otpCode);

    }


    /**
     * The Verifier.
     * Returns TRUE if valid, FALSE if invalid.
     * Throws Exception if user is blocked (Too many attempts).
     */
    @Transactional
    public boolean verifyOTP(String mail, String otpCode) {
        log.info("OTP Verification: Attempt for email: {}", mail);
        Optional<OtpVerification> otpVerificationOptional = otpVerificationRepo.findByEmail(mail);

        // 1. Check if OTP exists
        if (otpVerificationOptional.isEmpty()) {
            log.warn("OTP Verification: No active OTP record found for {}", mail);
            return false;
        }

        OtpVerification otp = otpVerificationOptional.get();

        // 2. Check Expiration
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP Verification: Code expired for {}", mail);
            return false;
        }

        // 3. SECURITY CHECK: Brute Force Protection
        // If they already failed 3 times, block them immediately.
        if (otp.getAttemptCount() >= maxAttempts) {
            log.warn("OTP Security: Brute force protection triggered for {}. Attempts: {}", mail, otp.getAttemptCount());
            throw new TooManyRequestsException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        // Increment the attempt counter (Audit Trail)
        otp.setAttemptCount(otp.getAttemptCount() + 1);

        // 4. Check if the code matches
        if (!otp.getOtpCode().equals(otpCode)) {

            log.info("OTP Verification: Failed attempt {} of {} for {}", otp.getAttemptCount(), maxAttempts, mail);
            // Failed attempt: Update the counter in DB and return false.
            otpVerificationRepo.save(otp);
            return false;
        }

        log.info("OTP Verification: SUCCESS for email: {}", mail);
        // 5. Success!
        // Delete the used OTP so it cannot be used again (Replay Attack Protection).
        otpVerificationRepo.delete(otp);
        return true;
    }


    // Wrapper for Resend (same logic as create)
    @Transactional
    public void resendOTP(String mail) {
        log.info("OTP Request: Resending for email: {}", mail);
        createAndSendOTP(mail);
    }


    // "Janitor" method called by ScheduleTasks
    @Transactional
    public void cleanExpiredOTPs() {
        log.info("Janitor: Cleaning up expired OTPs from database...");
        try {
            otpVerificationRepo.deleteByExpiresAtBefore(LocalDateTime.now());
            log.info(" OTP cleanup successful.");
        } catch (Exception e) {
            log.error(" Failed to clear expired OTPs: {}", e.getMessage(), e);        }
    }


}
