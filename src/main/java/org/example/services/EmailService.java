package org.example.services;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String body);
    void sendRegistrationEmail(String to, String token);
    void sendResendVerificationEmail(String to, String token);
}
