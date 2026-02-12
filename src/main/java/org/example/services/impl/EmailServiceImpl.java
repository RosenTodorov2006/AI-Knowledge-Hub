package org.example.services.impl;

import org.example.repositories.UserRepository;
import org.example.services.EmailService;
import org.example.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    private final UserService userService;

    public EmailServiceImpl(JavaMailSender mailSender, UserService userService) {
        this.mailSender = mailSender;
        this.userService = userService;
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("aiknowledgehuba@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendBulkReminder(String subject, String template, String link) {
        userService.findAllUsers().forEach(user -> {
            try {
                String name = user.getFullName() != null ? user.getFullName() : user.getUsername();
                String body = String.format(template, name, link);
                sendSimpleEmail(user.getEmail(), subject, body);
            } catch (Exception e) {
                logger.error("Failed to send reminder to {}: {}", user.getEmail(), e.getMessage());
            }
        });
    }
}
