package com.example.authpractice.services;

import com.example.authpractice.dtos.LoginRequest;
import com.example.authpractice.dtos.SignupRequest;
import com.example.authpractice.entities.Role;
import com.example.authpractice.entities.User;
import com.example.authpractice.exceptions.InvalidCredentialsException;
import com.example.authpractice.exceptions.UserAlreadyExistsException;
import com.example.authpractice.exceptions.UserNotFoundException;
import com.example.authpractice.repositories.UserRepo;
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
        User existingUser = userRepo.findByEmail(request.getEmail()).orElse(null);

        if(existingUser != null) {
           if (existingUser.isVerified()) {
               throw new UserAlreadyExistsException("Email Already Exists");
           }
            // Cleanup stale unverified account so we can reuse the email
           userRepo.delete(existingUser);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setVerified(false); // Must verify via OTP
        return userRepo.save(user);
    }


    /**
     * Validates Login Credentials.
     * * Includes Timing Attack Protection.
     */
    public User validateCredentials(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail()).orElse(null);

        // SCENARIO 1: User Not Found
        if (user == null) {
            // Run the fake check to waste time (simulate BCrypt calculation)
            passwordEncoder.matches(request.getPassword(), DUMMY_PASSWORD_HASH);
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        // SCENARIO 2: Password Mismatch
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        // SCENARIO 3: Email Not Verified
        if (!user.isVerified()) {
            throw new InvalidCredentialsException("User Not Verified");
        }
        return user;
    }


    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }


    // Called after successful OTP verification
    @Transactional
    public void markUserAsVerified(String email) {
        User user = findByEmail(email);
        user.setVerified(true);
         userRepo.save(user);
    }

    /**
     * Updates password and logs user out everywhere.
     * * SECURITY CRITICAL:
     * When a user changes their password, we MUST revoke their Refresh Tokens.
     * Otherwise, a hacker with an old token could stay logged in even after the owner changed the password.
     */
    @Transactional
    public void updatePassword(String email, String newPassword) {
         User user = findByEmail(email);
         user.setPassword(passwordEncoder.encode(newPassword));
         userRepo.save(user);
        // Kill all active sessions on other devices
         refreshTokenService.revokeAllTokensOfUser(user);
    }

    // Checks if user exists before sending Reset Password OTP
    @Transactional
    public void initiatePasswordReset(String email) {
        findByEmail(email); // Throws exception if not found
    }

    // Background job calls this to delete unverified accounts
    public void cleanUnverifiedUsers() {
        userRepo.deleteUnverifiedUsers(LocalDateTime.now().minusHours(24));
    }
}
