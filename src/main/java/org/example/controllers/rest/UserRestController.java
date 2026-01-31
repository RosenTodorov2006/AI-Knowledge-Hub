package org.example.controllers.rest;

import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.RegisterSeedDto;
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
import java.util.Map;
import java.util.stream.Collectors; // Добавен импорт

@RestController
@RequestMapping("/api/test/auth")
public class UserRestController {
    public static final String JSON_KEY_MESSAGE = "message";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_ERRORS = "errors";
    public static final String JSON_KEY_USERNAME = "username";
    private static final String MSG_KEY_REGISTER_SUCCESS = "auth.success.register";
    private static final String MSG_KEY_NOT_LOGGED_IN = "auth.error.not.logged.in";
    private final UserService userService;
    private final MessageSource messageSource;

    public UserRestController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> testRegister(@RequestBody @Valid RegisterSeedDto registerSeedDto,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        try {
            userService.register(registerSeedDto);
            String msg = messageSource.getMessage(MSG_KEY_REGISTER_SUCCESS, null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(JSON_KEY_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(Principal principal) {
        if (principal == null) {
            String msg = messageSource.getMessage(MSG_KEY_NOT_LOGGED_IN, null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, msg));
        }

        return ResponseEntity.ok(Map.of(JSON_KEY_USERNAME, principal.getName()));
    }

    private Map<String, List<String>> extractErrors(BindingResult bindingResult) {
        List<String> errors = bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return Map.of(JSON_KEY_ERRORS, errors);
    }
}