package org.example.schedulers;

import jakarta.transaction.Transactional;
import org.example.models.entities.UserEntity;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountDeactivationScheduler {

    private final UserService userService;

    public AccountDeactivationScheduler(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deactivateInactiveAccounts() {
        userService.deactivateInactiveUsers(4);
    }
}
