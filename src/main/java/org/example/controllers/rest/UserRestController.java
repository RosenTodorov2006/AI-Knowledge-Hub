package org.example.controllers.rest;

import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.models.dtos.importDtos.UserReactivateDto;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError; // Добавен импорт
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors; // Добавен импорт

@RestController
@RequestMapping("/api/auth")
public class UserRestController {
    private final UserService userService;
    private final MessageSource messageSource;

    public UserRestController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterSeedDto dto,
                                      BindingResult bindingResult,
                                      Locale locale) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        try {
            userService.register(dto);
            String msg = messageSource.getMessage("auth.success.register", null, locale);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        String result = userService.verifyUser(token);

        return switch (result) {
            case "SUCCESS" -> ResponseEntity.ok(Map.of("message", "Account verified successfully."));
            case "ALREADY_ACTIVE" -> ResponseEntity.ok(Map.of("info", "Account is already active."));
            case "EXPIRED" -> ResponseEntity.status(HttpStatus.GONE).body(Map.of("error", "Link expired."));
            default -> ResponseEntity.badRequest().body(Map.of("error", "Invalid token."));
        };
    }

    @PostMapping("/reactivate")
    public ResponseEntity<?> reactivate(@RequestBody @Valid UserReactivateDto dto,
                                        BindingResult bindingResult,
                                        Locale locale) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        boolean success = userService.reactivateAccount(dto.getEmail(), dto.getPassword());

        if (success) {
            String msg = messageSource.getMessage("reactivate.success.msg", null, locale);
            return ResponseEntity.ok(Map.of("message", msg));
        }

        String error = messageSource.getMessage("settings.error.password_mismatch", null, locale);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", error));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resend(@RequestParam("email") String email, Locale locale) {
        try {
            userService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("message", "Verification email resent to " + email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal, Locale locale) {
        if (principal == null) {
            String msg = messageSource.getMessage("auth.error.not.logged.in", null, locale);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    private Map<String, List<String>> extractErrors(BindingResult bindingResult) {
        return Map.of("errors", bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList()));
    }
}