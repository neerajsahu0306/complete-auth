package com.example.authpractice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


/**
 * DEV NOTE: The "ID Card" Checkpoint
 * ----------------------------------
 * This filter intercepts every HTTP request hitting our application.
 * It checks if the user brought their "ID Card" (The JWT Access Token).
 * * LOGIC FLOW:
 * 1. Request arrives (e.g., GET /api/user/hello).
 * 2. We check the "Authorization" header for a "Bearer <token>" string.
 * 3. If found, we crack open the token to see who it belongs to.
 * 4. If valid, we manually tell Spring Security: "This user is logged in. Let them pass."
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;


    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;

    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // Step 1: Check if the token exists
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            // If no token, pass the request along.
            // The SecurityConfig will decide if they get rejected (401) or allowed (public URLs).
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Step 2: Extract the token (Remove "Bearer " prefix)
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token); // 'sub' claim

            // Step 3: Check if user is not already authenticated in this context
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 4: Validate the token signature and expiration
                if( jwtService.isTokenValid(token, email)) {

                    // Optimization: We extract ID and Role directly from the token.
                    // This avoids a database call to 'userRepo.findByEmail()', making the API faster.
                    String userId = jwtService.extractUserId(token);
                    String role = jwtService.extractRole(token);

                    // Convert Role string (USER) to Spring Authority (ROLE_USER)
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                    // Create the "Authentication Ticket"
                    // IMPORTANT: We set 'userId' as the Principal, not the full User object.
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 5: Put the ticket in the Security Context
                    // Now, for the rest of this request, Spring knows who this user is.
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception e) {
            // If anything fails (expired token, bad signature), we log it.
            // In a real app, you might want to return a specific 401 error here.
            // Log the error but DO NOT throw it.
            // Ensure the context is clear so they are treated as anonymous.
            SecurityContextHolder.clearContext();
             System.err.println("JWT Verification Failed: " + e.getMessage()); // Optional debug
        }
        // Continue the request chain
        filterChain.doFilter(request, response);

    }
}
