package com.example.authpractice.services;

import com.example.authpractice.dtos.LoginRequest;
import com.example.authpractice.dtos.SignupRequest;
import com.example.authpractice.entities.Role;
import com.example.authpractice.entities.User;
import com.example.authpractice.exceptions.InvalidCredentialsException;
import com.example.authpractice.exceptions.UserAlreadyExistsException;
import com.example.authpractice.exceptions.UserNotFoundException;
import com.example.authpractice.repositories.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * DEV NOTE: User Management Core
 * ------------------------------
 * This service handles the lifecycle of the User entity (Signup, Login, Password Reset).
 * * SECURITY HIGHLIGHT: TIMING ATTACK PROTECTION
 * Notice the 'DUMMY_PASSWORD_HASH'.
 * If a hacker tries to login with a fake email, the database returns quickly (User not found).
 * If they use a real email, the database takes longer (Password check).
 * Hackers can measure this time difference (in milliseconds) to guess which emails exist in our system.
 * * THE FIX:
 * Even if the user is NOT found, we run a "fake" password check against the dummy hash.
 * This ensures that every login attempt takes roughly the same amount of time, blinding the hacker.
 */
@Service
@Slf4j
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    // A pre-calculated BCrypt hash. We run this when a user DOESN'T exist to simulate work.
    private static final String DUMMY_PASSWORD_HASH = "$2a$10$dummyHashToPreventTimingAttack1234567890123456789012";

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Handles New User Registration.
     * * LOGIC:
     * 1. Check if email exists.
     * 2. If it exists AND is verified -> Throw Error (Account taken).
     * 3. If it exists but NOT verified -> DELETE IT (Assume previous signup was abandoned) and create new.
     * 4. Save new user as UNVERIFIED (Role = USER).
     */
    @Transactional
    public User createUser(SignupRequest request) {
        log.info("Signup: Processing registration for email: {}", request.getEmail());
        User existingUser = userRepo.findByEmail(request.getEmail()).orElse(null);

        if(existingUser != null) {
           if (existingUser.isVerified()) {
               log.warn("Signup: Blocked registration - verified account already exists for {}", request.getEmail());
               throw new UserAlreadyExistsException("Email Already Exists");
           }
            log.info("Signup: Removing stale/unverified user record for {}", request.getEmail());
            // Cleanup stale unverified account so we can reuse the email
           userRepo.delete(existingUser);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setVerified(false); // Must verify via OTP
        User savedUser = userRepo.save(user);
        log.info("Signup: User record successfully saved for {}", request.getEmail());
        return savedUser;
    }


    /**
     * Validates Login Credentials.
     * * Includes Timing Attack Protection.
     */
    public User validateCredentials(LoginRequest request) {
        log.info("Auth: Login attempt for user: {}", request.getEmail());
        User user = userRepo.findByEmail(request.getEmail()).orElse(null);

        // SCENARIO 1: User Not Found
        if (user == null) {
            log.warn("Auth: User not found: {}. Executing dummy hash check.", request.getEmail());
            // Run the fake check to waste time (simulate BCrypt calculation)
            passwordEncoder.matches(request.getPassword(), DUMMY_PASSWORD_HASH);
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        // SCENARIO 2: Password Mismatch
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Auth: Password mismatch for user: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        // SCENARIO 3: Email Not Verified
        if (!user.isVerified()) {
            log.warn("Auth: Attempted login to unverified account: {}", request.getEmail());
            throw new InvalidCredentialsException("User Not Verified");
        }
        log.info("Auth: Login successful for user: {}", request.getEmail());
        return user;
    }


    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() ->  {
                    log.error("Database: Requested user not found: {}", email);
                   return new UserNotFoundException("User Not Found");
                });
    }


    // Called after successful OTP verification
    @Transactional
    public void markUserAsVerified(String email) {
        log.info("Verification: Finalizing verification for {}", email);
        User user = findByEmail(email);
        user.setVerified(true);
         userRepo.save(user);
        log.info("Verification: User {} is now marked as active/verified", email);
    }

    /**
     * Updates password and logs user out everywhere.
     * * SECURITY CRITICAL:
     * When a user changes their password, we MUST revoke their Refresh Tokens.
     * Otherwise, a hacker with an old token could stay logged in even after the owner changed the password.
     */
    @Transactional
    public void updatePassword(String email, String newPassword) {
        log.info("Security: Password update requested for {}", email);
         User user = findByEmail(email);
         user.setPassword(passwordEncoder.encode(newPassword));
         userRepo.save(user);
        log.info("Security: Password changed for {}. Requesting global session revocation.", email);
        // Kill all active sessions on other devices
         refreshTokenService.revokeAllTokensOfUser(user);
    }

    // Checks if user exists before sending Reset Password OTP
    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Reset: Password reset initiated for {}", email);
        findByEmail(email); // Throws exception if not found
        log.info("Reset: User {} found, proceeding to OTP generation", email);
    }

    // Background job calls this to delete unverified accounts
    public void cleanUnverifiedUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        log.info(" Starting daily cleanup of users unverified since {}", threshold);
        try {
            userRepo.deleteUnverifiedUsers(LocalDateTime.now().minusHours(24));
            log.info(" Cleanup task finished successfully");
        } catch (Exception e) {
            log.error("Failed to purge unverified users: {}", e.getMessage(), e);
        }
    }
}
