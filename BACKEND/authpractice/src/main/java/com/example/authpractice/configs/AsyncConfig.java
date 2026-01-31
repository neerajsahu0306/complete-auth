package com.example.authpractice.configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 *  DEV NOTE:  Async Configuration
 * -----------------------------
 * This class is all about performance. By default, Spring runs everything synchronously (blocking the main thread).
 * In our app, we have heavy tasks like "Sending Emails" (OTP, Welcome mails).
 * We don't want the user to stare at a loading screen while we wait for the mail server to respond.
 * So, we enable "@EnableAsync" to tell Spring: "Hey, run these heavy tasks in a separate background thread."
 */
@Configuration
@EnableAsync  // Triggers Spring to look for methods annotated with @Async and run them in background
public class AsyncConfig {

    /**
     * This method defines our "Worker Pool" (The Thread Pool).
     * Think of this as hiring a team of background workers to handle emails.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
       ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 1. Core Pool Size:
        // We keep 5 workers active at all times, even if they are idle.
        // They are ready to pick up tasks instantly.
        executor.setCorePoolSize(5);

        // 2. Max Pool Size:
        // If the load gets crazy high (queue is full), we can hire up to 10 workers temporarily.
        executor.setMaxPoolSize(10);

        // 3. Queue Capacity:
        // If the 5 core workers are busy, put new tasks in this waiting line (Queue).
        // We can hold 100 pending emails here before we need to spawn more threads (up to MaxPoolSize).
        executor.setQueueCapacity(100);

        // 4. Thread Name Prefix:
        // When debugging logs, we'll see names like "async-email-1", "async-email-2".
        // Helps us know exactly which thread crashed if something goes wrong.
        executor.setThreadNamePrefix("async-email-");

        executor.initialize();
        return executor;

    }
}
