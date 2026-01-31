package com.example.authpractice.services;

import com.example.authpractice.entities.AuthProvider;
import com.example.authpractice.entities.Role;
import com.example.authpractice.entities.User;
import com.example.authpractice.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DEV NOTE: OAuth Business Logic (The Merger)
 * -------------------------------------------
 * This service decides what to do when a user successfully logs in via Google.
 * * It handles 3 scenarios:
 * 1. RETURNING USER: User has logged in with Google before. -> Log them in.
 * 2. NEW USER: User has never been here. -> Create new account.
 * 3. LINKING (The Tricky One): User has a LOCAL account (email/password) but
 * decides to click "Sign in with Google" this time.
 * -> We detect the matching email and "upgrade" their account to support Google login.
 */
@Service
@RequiredArgsConstructor
public class OAuthService {
    
    private final UserRepo userRepository;


    /**
     * The Main Entry Point.
     * Called by OAuthSuccessHandler after Google says "Yes, this is a valid user".
     */
    @Transactional
    public User processOAuthLogin(OAuth2User oAuth2User, AuthProvider provider) {
        // Extract email and the unique ID (sub) from Google's response
        String email = oAuth2User.getAttribute("email");
        String oauthId = oAuth2User.getAttribute("sub");
        
        if (email == null || oauthId == null) {
            throw new IllegalArgumentException("Email and OAuth ID are required from OAuth provider");
        }

        // STRATEGY 1: Search by the Stable OAuth ID
        // The 'sub' ID never changes, even if the user changes their email address on Google.
        return userRepository.findByOauthIdAndAuthProvider(oauthId, provider)
                .map(existingUser -> updateEmailIfChanged(existingUser, email))
                .orElseGet(() -> findOrCreateUser(email, oauthId, provider));
    }

    // Updates the email in our DB if the user changed it in their Google settings.
    private User updateEmailIfChanged(User user, String newEmail) {
        if (!user.getEmail().equals(newEmail)) {
            user.setEmail(newEmail);
            return userRepository.save(user);
        }
        return user;
    }
    
    private User findOrCreateUser(String email, String oauthId, AuthProvider provider) {
        // STRATEGY 2: Search by Email (Account Linking)
        // If we didn't find them by OAuth ID, maybe they have an old "Email/Password" account?
        return userRepository.findByEmail(email)
                .map(existingUser -> linkOAuthAccount(existingUser, oauthId, provider))
                .orElseGet(() -> createNewOAuthUser(email, oauthId, provider));
    }

    /**
     * Handles the "Merger" of a Local account into an OAuth account.
     */
    private User linkOAuthAccount(User existingUser, String oauthId, AuthProvider provider) {
        // Security Check:
        // If this user is ALREADY linked to another provider (e.g., GitHub), we block it.
        // We generally don't want one user account linked to 5 different OAuth providers to avoid confusion.
        if (existingUser.getOauthId() != null) {
            throw new IllegalStateException("This email is already linked to another OAuth provider");
        }

        // The Linking Logic:
        // We add the Google ID to their existing row.
        // We also mark them as VERIFIED (because we trust Google).
        existingUser.setOauthId(oauthId);
        existingUser.setAuthProvider(provider);
        existingUser.setVerified(true);
        
        return userRepository.save(existingUser);
    }

    // Purely new user creation
    private User createNewOAuthUser(String email, String oauthId, AuthProvider provider) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setOauthId(oauthId);
        newUser.setAuthProvider(provider);
        newUser.setRole(Role.USER);
        newUser.setVerified(true); // Trust Google
        newUser.setPassword(null); // They don't need a password
        
        return userRepository.save(newUser);
    }
}
