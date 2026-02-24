package org.example.services.impl;

import org.example.repositories.UserRepository;
import org.example.services.EmailService;
import org.example.services.UserService;
import org.example.utils.VerificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final String BASE_URL = "https://ai-knowledge-app.yellowhill-b3aceaa2.northeurope.azurecontainerapps.io";
    private final JavaMailSender mailSender;
    private final VerificationUtil verificationUtil;

    public EmailServiceImpl(JavaMailSender mailSender, VerificationUtil verificationUtil) {
        this.mailSender = mailSender;
        this.verificationUtil = verificationUtil;
    }

    @Async
    @Override
    public void sendRegistrationEmail(String to, String token) {
        String link = verificationUtil.buildConfirmationLink(BASE_URL, token);
        String subject = "Confirm your registration";
        String body = "Welcome!\n\nPlease confirm your account using the link below:\n" + link;
        sendSimpleEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendResendVerificationEmail(String to, String token) {
        String link = verificationUtil.buildConfirmationLink(BASE_URL, token);
        String subject = "Resend: Confirm your registration";
        String body = "Here is your new confirmation link:\n\n" + link;
        sendSimpleEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("aiknowledgehuba@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
