package com.example.authpractice.dtos;

import com.example.authpractice.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DEV NOTE: Login Success Payload
 * -------------------------------
 * This is the JSON object we send back to React when a user successfully logs in.
 * * Payload Structure:
 * {
 * "email": "user@gmail.com",
 * "role": "USER",
 * "accessToken": "eyJhbGci...",  // React puts this in the 'Authorization' header
 * "message": "Login Successful"
 * }
 * * NOTE: We do NOT send the Refresh Token here. That goes in an HttpOnly Cookie.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {


    private String email;

    private Role role;

    private String accessToken;

    private String message;

    // Helper constructor for quick success responses
    public AuthResponse(String email, Role role, String accessToken) {
        this.email = email;
        this.role = role;
        this.accessToken = accessToken;
    }
}
