package org.example.services;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String body);
    void sendBulkReminder(String subject, String template, String link);
}
