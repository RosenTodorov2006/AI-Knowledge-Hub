package org.example.schedulers;

import org.example.repositories.UserRepository;
import org.example.services.EmailService;
import org.example.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppTestingReminderTask {

    private static final Logger logger = LoggerFactory.getLogger(AppTestingReminderTask.class);

    private static final String APP_LINK = "https://ai-knowledge-app.yellowhill-b3aceaa2.northeurope.azurecontainerapps.io";
    private static final String REMINDER_SUBJECT = "Reminder: Test the new AI features in the Hub!";
    private static final String REMINDER_BODY_TEMPLATE =
            "Hello, %s!\n\n" +
                    "It has been 24 hours since our last update. Your participation is critical for the application's stability!\n\n" +
                    "Please take 2 minutes to:\n" +
                    "1. Upload a PDF document for vectorization.\n" +
                    "2. Ask a question in the chat based on that document.\n\n" +
                    "Access the application here: %s\n\n" +
                    "Thank you for helping us improve!\n\n" +
                    "Best regards,\nThe AI Knowledge Hub Team";

    private final EmailService emailService;
    private final UserService userService;

    public AppTestingReminderTask(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    @Scheduled(fixedRate = 86400000)
    public void executeDailyTestingReminder() {
        logger.info("Starting daily user reminder task...");

        userService.findAllUsers().forEach(user -> {
            try {
                String name = user.getFullName() != null ? user.getFullName() : user.getUsername();
                String body = String.format(REMINDER_BODY_TEMPLATE, name, APP_LINK);

                emailService.sendSimpleEmail(user.getEmail(), REMINDER_SUBJECT, body);
            } catch (Exception e) {
                logger.error("Failed to send reminder to {}: {}", user.getEmail(), e.getMessage());
            }
        });
    }
}