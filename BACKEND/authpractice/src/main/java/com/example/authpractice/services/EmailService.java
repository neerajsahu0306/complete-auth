package com.example.authpractice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


/**
 * DEV NOTE: Email Dispatcher
 * --------------------------
 * Handles all outbound email communications.
 * * PERFORMANCE CRITICAL:
 * Notice the @Async annotation. Sending an email via SMTP (Gmail/Outlook)
 * can take 2-5 seconds. We do NOT want the user to stare at a loading spinner
 * for that long just to get an OTP.
 * * This service hands off the task to a background thread (configured in AsyncConfig)
 * so the Controller can return "Success" immediately.
 */
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    private final String fromEmail;

    // We inject the sender email from application.properties so it's easy to change later.
    public EmailService( JavaMailSender javaMailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }


    /**
     * Sends the 6-digit verification code.
     * * @Async("taskExecutor"):
     * Tells Spring: "Don't run this on the main HTTP thread.
     * Throw it into our 'async-email-' thread pool."
     */
    @Async("taskExecutor")
    public void sendOTPEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromEmail);
            simpleMailMessage.setTo(toEmail);
            simpleMailMessage.setSubject("OTP for Authentication");
            simpleMailMessage.setText("Your OTP code is: " + otp +
                    "\n\nThis code will expire in 5 minutes." +
                    "\n\nIf you didn't request this, please ignore this email.");

            javaMailSender.send(simpleMailMessage);

        } catch (RuntimeException e) {
            // If email fails (e.g., bad internet, daily quota exceeded), we log it.
            // Since this is async, the user won't see an error page, but they won't get the email.
            // In a pro app, you might want to retry this or log to a monitoring service.
            System.err.println("Failed to send OTP to " + toEmail + ": " + e.getMessage());

        }
    }

    /**
     * Sends a friendly greeting after successful email verification.
     * Also, Async, because strictly speaking, the user doesn't need to wait for this
     * to start using the app.
     */
    @Async("taskExecutor")
    public void sendWelcomeMail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to AuthPractice!");
            message.setText("Thank you for verifying your account. Welcome aboard!");

            javaMailSender.send(message);

        } catch (RuntimeException e) {
            System.err.println("Failed to send welcome email to " + toEmail + ": " + e.getMessage());
        }
    }



}
