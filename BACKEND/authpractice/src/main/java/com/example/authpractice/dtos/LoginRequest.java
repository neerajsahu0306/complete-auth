package com.example.authpractice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DEV NOTE: Simple Login Payload
 * ------------------------------
 * Used for the /api/auth/login endpoint.
 * Note: We don't enforce the complex password regex here.
 * Why? If we change our password policy later, we don't want to block old users
 * from logging in just because their old password doesn't meet the new regex.
 * We just check if it's not blank.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}
