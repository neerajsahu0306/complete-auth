package com.example.authpractice.entities;

/**
 * DEV NOTE: Authentication Provider
 * ---------------------------------
 * This Enum tells the backend "Who do we trust to verify this user?"
 * * Scenarios:
 * 1. LOCAL:
 * - The user typed a password in OUR login form.
 * - We MUST validate the password hash in our database.
 * * 2. GOOGLE:
 * - The user clicked "Sign in with Google".
 * - Google verified them. We trust Google.
 * - We SKIP password validation (password is usually null).
 */
public enum AuthProvider {
    LOCAL,  // Standard Email/Password signup
    GOOGLE, // OAuth2 signup
}
