package org.example.schedulers;

import org.example.repositories.UserRepository;
import org.example.services.EmailService;
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

    public AppTestingReminderTask(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 86400000)
    public void executeDailyTestingReminder() {
        logger.info("Starting daily user reminder task via EmailService...");
        emailService.sendBulkReminder(REMINDER_SUBJECT, REMINDER_BODY_TEMPLATE, APP_LINK);
    }
}