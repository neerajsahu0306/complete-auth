package com.example.authpractice.configs;


import com.example.authpractice.security.JwtAuthenticationFilter;
import com.example.authpractice.security.OAuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


/**
 * DEV NOTE: The Security Gatekeeper
 * ---------------------------------
 * This is where we configure the "Rules of Engagement" for our API.
 * Spring Security is powerful but defaults to blocking everything.
 * We use this config to:
 * 1. Open specific doors (CORS for React, Public Endpoints).
 * 2. Close others (Require Auth for /api/**).
 * 3. Plug in our custom logic (JWT Filter, OAuth Handler).
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuthSuccessHandler oAuthSuccessHandler;


    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, OAuthSuccessHandler oAuthSuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;

        this.oAuthSuccessHandler = oAuthSuccessHandler;
    }

    /**
     * The Security Filter Chain (The Rules).
     * Think of this as a checklist every request must pass through.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, CorsConfigurationSource corsConfigurationSource) throws Exception {
        httpSecurity
                // 1. CORS: Allow our React Frontend (localhost:5173) to talk to us.
                // Without this, the browser will block the request before it even reaches logic.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF: Disabled.
                // We disable this because we are using Stateless JWTs.
                // CSRF is mainly for session-based apps where the browser auto-sends cookies.
                // Since our Access Token is in the Header (not a cookie), we are safe.

                .csrf(csrf -> csrf.disable())

                // 3. Exception Handling:
                // If an unauthorized user tries to access a protected route, just return 401.
                // Default Spring behavior is to redirect to an HTML login page (bad for React Apps).
                .exceptionHandling(customizer ->
                        customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // 4. Authorization Rules (The VIP List):
                .authorizeHttpRequests(auth -> auth
                        // Public routes: Login, Signup, OAuth, etc.
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/**").permitAll()
                        // Admin only routes
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Everything else requires a valid token
                        .anyRequest().authenticated())

                // 5. Session Management: STATELESS.
                // Crucial! This tells Spring: "Do NOT create a JSESSIONID cookie."
                // We want every request to be independent and authenticated via Token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 6. OAuth2 Login (Google):
                // If login succeeds, run our custom 'oAuthSuccessHandler' to generate tokens.
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuthSuccessHandler))

                // 7. Custom Filter Injection:
                // Add our 'JwtAuthenticationFilter' BEFORE the standard Username/Password check.
                // This allows us to log users in via Token before Spring tries to look for a password.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }


    /**
     * CORS Configuration (The Bridge).
     * Specifically allows our React app to send Cookies (AllowCredentials=true)
     * and headers to this backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // The exact URL of your React App
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "https://auth-practice-app.netlify.app"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        // Critical: Allows sending HttpOnly Cookies (Refresh Token) across origins
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache these settings for 1 hour
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
