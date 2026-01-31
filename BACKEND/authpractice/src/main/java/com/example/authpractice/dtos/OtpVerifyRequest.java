package com.example.authpractice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DEV NOTE: OTP Verification Payload
 * ----------------------------------
 * Used when the user enters the 6-digit code from their email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // We strictly enforce 6 characters. Not 5, not 7.
    // This helps the frontend show an error immediately if the user missed a digit.
   @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;
}
