package com.example.authpractice.controllers;

import com.example.authpractice.dtos.*;
import com.example.authpractice.entities.RefreshToken;
import com.example.authpractice.entities.User;
import com.example.authpractice.security.CookieUtil;
import com.example.authpractice.security.JwtService;
import com.example.authpractice.services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final OTPService otpService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final CookieUtil cookieUtil;


    public AuthController(UserService userService, OTPService otpService, JwtService jwtService, RefreshTokenService refreshTokenService, EmailService emailService, CookieUtil cookieUtil) {
        this.userService = userService;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.cookieUtil = cookieUtil;
    }

    // --- SIGN UP FLOW ---

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signUp(@Valid @RequestBody SignupRequest request) {
        // 1. Create the user (Unverified)
        userService.createUser(request);
        // 2. Send the code (Async)
        otpService.createAndSendOTP(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Signup successful. Please check your email for OTP."));

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request, HttpServletResponse response) {
        // 1. Validate Code
        boolean isValid = otpService.verifyOTP(request.getEmail(), request.getOtp());

        if (!isValid) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, "Invalid OTP"));
        }
        // 2. Mark Verified
        userService.markUserAsVerified(request.getEmail());

        // 3. AUTO-LOGIN
        // Instead of asking them to login again, we generate tokens immediately.
        User user = userService.findByEmail(request.getEmail());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        // 4. Set HttpOnly Cookie
       Cookie cookie= cookieUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(cookie);

        // 5. Send Welcome Email (Async)
        emailService.sendWelcomeMail(user.getEmail());

        return ResponseEntity.ok(new AuthResponse(user.getEmail(), user.getRole(), accessToken));
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponse> resendOtp(@RequestBody OtpVerifyRequest request) {
        otpService.resendOTP(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP resent successfully. Please check your email."));
    }

    // --- LOGIN FLOW ---

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response ) {
        // 1. Validate Password (and timing attack protection)
        User user = userService.validateCredentials(request);

        // 2. Generate Tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        // 3. Secure Cookie Delivery
        Cookie cookie= cookieUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(cookie);
        return ResponseEntity.ok(new AuthResponse(user.getEmail(), user.getRole(), accessToken));
    }

    // --- SESSION MANAGEMENT ---

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        // 1. Validate incoming cookie
        RefreshToken token = refreshTokenService.validateAndGetToken(refreshToken);
        User user = token.getUser();

        // 2. Rotate Token (Delete old, create new)
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, user);
        String newAccessToken = jwtService.generateAccessToken(user);

        // 3. Set NEW Cookie
        Cookie cookie = cookieUtil.createRefreshTokenCookie(newRefreshToken);
        response.addCookie(cookie);

        return ResponseEntity.ok(new AuthResponse(user.getEmail(), user.getRole(), newAccessToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {

        // 1. Remove from DB (so it can't be used again)
        refreshTokenService.revokeToken(refreshToken);

        // 2. Clear from Browser
        Cookie cookie = cookieUtil.createLogoutCookie();
        response.addCookie(cookie);
         return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        otpService.createAndSendOTP(request.getEmail());
        return ResponseEntity.ok("OTP sent successfully. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        boolean isValid = otpService.verifyOTP(request.getEmail(), request.getOtp());

        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }

        userService.updatePassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully.");
    }
}
