package org.example.controllers.rest;

import jakarta.validation.Valid;
import org.example.exceptions.InvalidPasswordException;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.UserDeactivateDto;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

@RestController
@RequestMapping("/api/settings")
public class SettingsRestController {
    private final UserService userService;
    private final MessageSource messageSource;

    public SettingsRestController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettingsData(Principal principal) {
        return ResponseEntity.ok(assembleSettingsData(principal.getName()));
    }

    @PostMapping("/change-info")
    public ResponseEntity<?> changeInfo(@Valid @RequestBody ChangeProfileDto dto,
                                        BindingResult bindingResult,
                                        Principal principal,
                                        Locale locale) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        try {
            userService.changeProfileInfo(dto, principal.getName());
            return ResponseEntity.ok(userService.getUserViewByEmail(dto.getEmail()));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", messageSource.getMessage(e.getMessage(), null, locale)));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangeUserPasswordDto dto,
                                            BindingResult bindingResult,
                                            Principal principal,
                                            Locale locale) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        try {
            userService.changeUserPassword(dto, principal.getName());
            String successMsg = messageSource.getMessage("settings.success.password", null, locale);
            return ResponseEntity.ok(Map.of("message", successMsg));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", messageSource.getMessage(e.getMessage(), null, locale)));
        }
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(@Valid @RequestBody UserDeactivateDto dto,
                                               Principal principal,
                                               Locale locale) {
        try {
            userService.disableUser(principal.getName(), dto.getCurrentPassword());
            return ResponseEntity.ok(Map.of("message", "Account deactivated successfully."));
        } catch (InvalidPasswordException e) {
            String errorMsg = messageSource.getMessage(e.getMessage(), null, locale);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", errorMsg));
        }
    }

    @PostMapping("/toggle-emails")
    public ResponseEntity<?> toggleEmails(Principal principal, Locale locale) {
        try {
            userService.toggleEmailNotifications(principal.getName());
            return ResponseEntity.ok(Map.of("message", "Email preferences updated."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Map<String, Object> assembleSettingsData(String email) {
        return Map.of(
                "currentUser", userService.getUserViewByEmail(email),
                "profileDto", userService.getChangeProfileDto(email),
                "passwordDto", new ChangeUserPasswordDto()
        );
    }

    private Map<String, List<String>> extractErrors(BindingResult bindingResult) {
        return Map.of("errors", bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList()));
    }
}
