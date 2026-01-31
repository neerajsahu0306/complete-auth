package com.example.authpractice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * DEV NOTE: Password Encryption Setup
 * -----------------------------------
 * This class is responsible for protecting user passwords.
 * Rule #1 of Auth: NEVER store passwords in plain text.
 * * We use BCrypt, which is a hashing algorithm specifically designed for passwords.
 * It's slow on purpose (to prevent brute-force attacks) and handles "salting" automatically.
 */

@Configuration
public class PasswordConfig {

    /**
     * Registers the PasswordEncoder in the Spring Context.
     * * Why do we need this @Bean?
     * So that in our 'UserService', we can just write:
     * @Autowired private PasswordEncoder encoder;
     * And Spring will verify that this 'BCryptPasswordEncoder' is the implementation to use.
     * * Fun Fact: BCrypt generates a different hash for the same password every time
     * (because of random salts), but the .matches() method is smart enough to figure it out.
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // Default strength is 10. Higher = slower login but harder to crack.
    }
}
