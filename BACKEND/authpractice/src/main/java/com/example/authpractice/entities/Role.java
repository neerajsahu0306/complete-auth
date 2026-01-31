package com.example.authpractice.entities;

/**
 * DEV NOTE: User Roles (RBAC)
 * ---------------------------
 * This Enum defines the "Authority" levels in our app.
 * * Usage:
 * - Stored in the 'user' table as a String ("USER", "ADMIN").
 * - Loaded into Spring Security's 'GrantedAuthority' list during login.
 * * Rules (from SecurityConfig):
 * - USER: Can access standard protected routes (/api/user/**, /dashboard).
 * - ADMIN: Can access everything, including sensitive endpoints (/api/admin/**).
 */
public enum Role {
    USER,  // The default role for new signups.
    ADMIN  // The "Super User". Usually assigned manually in the DB for safety.
}
