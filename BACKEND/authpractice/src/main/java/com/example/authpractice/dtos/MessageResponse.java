package com.example.authpractice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DEV NOTE: Generic Text Response
 * -------------------------------
 * This is the "General Purpose" wrapper for our API responses.
 * * WHY USE THIS?
 * Instead of returning a raw String (which can be hard for React to parse if it expects JSON),
 * we wrap everything in a JSON object: { "message": "..."}
 * * USAGE EXAMPLES:
 * - "OTP sent successfully."
 * - "Password has been reset."
 * - "User logged out."
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;
}
