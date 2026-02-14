package com.example.authpractice.exceptions;

import com.example.authpractice.dtos.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


/**
 * DEV NOTE: The Central Error Handler
 * ---------------------------------
 * This class acts as a global "Safety Net" for all Controllers.
 * * HOW IT WORKS:
 * If ANY Controller throws an exception, this class intercepts it.
 * Instead of showing a messy Stack Trace to the user, we return a clean JSON response.
 * * @RestControllerAdvice: Tells Spring to apply this logic to all Controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 409 Conflict: Good for duplicates (e.g., User tries to sign up with existing email)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUserExists(UserAlreadyExistsException e){
        log.warn("Conflict: Attempted registration with existing email - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
    }


    // 401 Unauthorized: Login failed or bad password
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<MessageResponse> handleInvalidCredentials(InvalidCredentialsException e){
        log.warn("Unauthorized: Login failure - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
    }



    // 401 Unauthorized: JWT is dead
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<MessageResponse> handleTokenExpired(TokenExpiredException e){
        log.warn("Unauthorized: Token expired event - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
    }


    // 404 Not Found: Specific entity missing
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Not Found: Resource missing - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    // 429 Too Many Requests: Rate limiting triggered (OTP spam)
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<MessageResponse> handleTooManyRequests(TooManyRequestsException e) {
        log.warn("Security Alert: Throttling triggered - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new MessageResponse(e.getMessage()));
    }


    // 403 Forbidden: User is logged in, but doesn't have the 'ADMIN' role
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Security Alert: Unauthorized access attempt - {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Access Denied: You do not have permission to access this resource"));
    }

    /**
     * VALIDATION ERROR HANDLER (400 Bad Request)
     * * Triggered when @Valid fails in DTOs (e.g., invalid email format, password too short).
     * * returns: A Map of field names and error messages.
     * Example: { "email": "Must be a Gmail address", "password": "Too short" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.info("Validation Failure: Request contained {} invalid fields", errors.size());
        return ResponseEntity.badRequest().body(errors);
    }


    /**
     * GENERIC CATCH-ALL (500 Internal Server Error)
     * * Triggered by NullPointerException, Database Connection Failures, etc.
     * * SECURITY NOTE:
     * We do NOT send the exception message to the user ("ex.getMessage()") because it might
     * reveal sensitive DB info. We just say "An unexpected error occurred".
     * We DO print the real error to the System.err console so the developer can fix it.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
        log.error("Unhandled Exception caught by Global Guard: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An unexpected error occurred"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        log.error("Runtime Exception: {}", e.getMessage(), e);
        Map<String , Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}
