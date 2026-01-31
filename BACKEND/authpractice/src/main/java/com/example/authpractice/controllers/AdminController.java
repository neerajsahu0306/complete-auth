package com.example.authpractice.controllers;

import com.example.authpractice.dtos.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * DEV NOTE: Protected Routes
 * --------------------------
 * These endpoints are protected by 'JwtAuthenticationFilter'.
 * If the user has a valid Token, the 'Authentication' object will be available.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/hello")
    public ResponseEntity<MessageResponse> sayHello(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(new MessageResponse("Hello User " + userId));
    }
}
