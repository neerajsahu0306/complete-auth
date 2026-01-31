package com.example.authpractice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    //  We are strictly enforcing Gmail accounts only.
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$",
            message = "Only Gmail accounts are allowed"
    )
    private String email;

    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotBlank(message = "Password is Required")
    /**
     * PASSWORD STRENGTH REGEX EXPLAINED:
     * ^                 : Start
     * (?=.*[a-z])       : Must contain at least 1 lowercase letter
     * (?=.*[A-Z])       : Must contain at least 1 uppercase letter
     * (?=.*\\d)         : Must contain at least 1 number
     * (?=.*[^A-Za-z\\d]): Must contain at least 1 special char (!@#$ etc.)
     * .{8,}             : Must be at least 8 chars long
     */
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "Password must be at least 8 characters with uppercase, lowercase, number and special character"
    )
    private String newPassword;
}
