package com.example.authpractice.security;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

/**
 * DEV NOTE: The "Cookie Factory"
 * ------------------------------
 * This utility handles the creation of the HTTP-Only Cookie for the Refresh Token.
 * * WHY NOT LOCAL STORAGE?
 * Storing tokens in localStorage is dangerous because any JS script (XSS attack) can read them.
 * * THE SOLUTION:
 * We store the Refresh Token in a special "HttpOnly" cookie.
 * - Browser automatically sends it with every request to our backend.
 * - JavaScript (React) CANNOT read/access this cookie.
 * - This makes the refresh token immune to XSS theft.
 */
@Component
public class CookieUtil {

    private static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/";
    private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 7; // 7 Days (in seconds)


    /**
     * Creates the Secure Cookie containing the Refresh Token.
     * * Security Flags Explained:
     * 1. HttpOnly: true -> JS cannot read this (Blocks XSS).
     * 2. Secure: true -> Browser only sends this over HTTPS (Blocks Man-in-the-Middle).
     * 3. Path: "/" -> Available for all endpoints (or restrict to /api/auth/refresh).
     * 4. MaxAge: 7 Days -> Keeps the user logged in for a week.
     * 5. SameSite: Strict -> Cookie is only sent for first-party requests (Blocks CSRF).
     */
    public Cookie createRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // NOTE: Requires HTTPS (or localhost) to work!
        cookie.setPath(COOKIE_PATH);
        cookie.setAttribute("sameSite", "none");
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        return cookie;
    }


    /**
     * "Deletes" the cookie by overwriting it with a dead one.
     * * How Cookie Deletion Works:
     * You can't actually "delete" a cookie from the server.
     * Instead, you send a new cookie with the SAME name but:
     * 1. Empty value ("")
     * 2. MaxAge = 0 (Expires immediately)
     * The browser sees this and removes the old one.
     */
    public Cookie createLogoutCookie() {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(COOKIE_PATH);
        cookie.setAttribute("sameSite", "none");
        cookie.setMaxAge(0); // 0 = Delete immediately
        return cookie;
    }
}
