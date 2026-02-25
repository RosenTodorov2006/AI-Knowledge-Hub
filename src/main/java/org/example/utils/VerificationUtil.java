package org.example.utils;

import org.example.models.entities.UserEntity;
import org.example.models.entities.VerificationTokenEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VerificationUtil {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    public void refreshPlaceholderToken(VerificationTokenEntity tokenEntity, UserEntity user, String token) {
        tokenEntity.setToken(token);
        tokenEntity.setUser(user);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
    }

    public boolean isTokenExpired(VerificationTokenEntity tokenEntity) {
        return tokenEntity.getExpiryDate().isBefore(LocalDateTime.now());
    }

    public String buildConfirmationLink(String baseUrl, String token) {
        return String.format("%s/users/verify?token=%s", baseUrl, token);
    }
}
