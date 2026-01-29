package org.example.controllers.rest;

import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.services.UserService;
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

    public static final String MSG_REGISTRATION_SUCCESS = "User registered successfully!";
    public static final String MSG_NOT_LOGGED_IN = "No user is currently logged in.";

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> testRegister(@RequestBody @Valid RegisterSeedDto registerSeedDto,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        try {
            userService.register(registerSeedDto);
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, MSG_REGISTRATION_SUCCESS));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(JSON_KEY_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(JSON_KEY_MESSAGE, MSG_NOT_LOGGED_IN));
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