package com.example.authpractice.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DEV NOTE: Registration Form Data
 * --------------------------------
 * Validates the raw JSON coming from the Sign-Up page.
 * * Spring Validation (@Valid):
 * If any of these constraints fail, Spring throws a MethodArgumentNotValidException
 * BEFORE the code even reaches the UserService. This protects our logic from bad data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class SignupRequest {

    @NotBlank(message = "Email is Required")
    @Email(message = "Email should be valid")
    //  We are strictly enforcing Gmail accounts only.
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$",
            message = "Only Gmail accounts are allowed"
    )
    private String email;

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
    private String password;
}
