package com.example.authpractice.security;




import com.example.authpractice.entities.AuthProvider;
import com.example.authpractice.entities.User;
import com.example.authpractice.services.OAuthService;
import com.example.authpractice.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;


/**
 * DEV NOTE: OAuth2 Success Handler (The Handoff)
 * ----------------------------------------------
 * This class triggers ONLY after Google (or another provider) successfully verifies the user.
 * * THE CHALLENGE:
 * The user is currently on the Backend URL (redirected from Google).
 * We need to send them back to the React Frontend (localhost:5173) WITH their tokens.
 * * THE SOLUTION:
 * 1. Process the user (save/update in DB).
 * 2. Generate Tokens (Access + Refresh).
 * 3. Set the Refresh Token as an HttpOnly Cookie (secure).
 * 4. Redirect the browser to React, attaching the Access Token in the URL query params.
 */
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthService oAuthService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    // Configured in application.properties. usually "http://localhost:5173/auth/callback"
    @Value("${app.oauth.redirect-uri:http://localhost:5173/auth/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            // Step 1: Extract User Data from Google's response
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Step 2: Figure out if this is Google, GitHub, etc.
            AuthProvider provider = determineProvider(request);

            // Step 3: Save or Update user in our database
            User user = oAuthService.processOAuthLogin(oAuth2User, provider);

            // Step 4: Generate our own Security Tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user);

            // Step 5: Secure the Refresh Token in an HttpOnly Cookie
            // (Browser will automatically store this)
            Cookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshCookie);

            // Step 6: Construct the Redirect URL
            // We attach the 'access' token to the URL so React can read it.
            // URL becomes: http://localhost:5173/auth/callback?access=eyJhbGci...
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("access", accessToken)
                    .build()
                    .toUriString();

            // Step 7: Send the user back to the Frontend
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            // Safety Net: If anything fails, don't crash.
            // Redirect to frontend with an error message so we can show a nice UI alert.
            if (!response.isCommitted()) {
                String errorUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                        .queryParam("error", "oauth_failed")
                        .queryParam("message", e.getMessage())
                        .build()
                        .toUriString();

                getRedirectStrategy().sendRedirect(request, response, errorUrl);
            }
        }
    }

    // Helper logic to detect which button the user clicked based on the URL.
    private AuthProvider determineProvider(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        if (requestUri.contains("/google")) {
            return AuthProvider.GOOGLE;
        }  // Future proofing for other providers
        /*else if (requestUri.contains("/github")) {
            return AuthProvider.GITHUB;
        } else if (requestUri.contains("/facebook")) {
            return AuthProvider.FACEBOOK;
        }*/

        return AuthProvider.GOOGLE;
    }
}