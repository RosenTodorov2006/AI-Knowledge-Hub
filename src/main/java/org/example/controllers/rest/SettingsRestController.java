package org.example.controllers.rest;

import jakarta.validation.Valid;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

@RestController
@RequestMapping("/api/settings")
public class SettingsRestController {
    public static final String JSON_KEY_MESSAGE = "message";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_ERRORS = "errors";

    public static final String MSG_PASSWORD_UPDATED = "Password updated successfully! Please use your new password for the next login.";
    public static final String MSG_ACCOUNT_DELETED = "Account deleted successfully!";

    private final UserService userService;

    public SettingsRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/changeInfo")
    public ResponseEntity<Object> changeInfo(@RequestBody @Valid ChangeProfileDto changeProfileDto,
                                             BindingResult bindingResult,
                                             Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        userService.changeProfileInfo(changeProfileDto, principal.getName());
        UserViewDto updatedData = userService.getUserViewByEmail(changeProfileDto.getEmail());

        return ResponseEntity.ok(updatedData);
    }

    @PostMapping("/changeUserPassword")
    public ResponseEntity<Object> changeUserPassword(@RequestBody @Valid ChangeUserPasswordDto changeUserPasswordDto,
                                                     BindingResult bindingResult,
                                                     Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(extractErrors(bindingResult));
        }

        try {
            userService.changeUserPassword(changeUserPasswordDto, principal.getName());
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, MSG_PASSWORD_UPDATED));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(JSON_KEY_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> delete(Principal principal) {
        try {
            userService.deleteUser(principal.getName());
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, MSG_ACCOUNT_DELETED));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(JSON_KEY_ERROR, e.getMessage()));
        }
    }

    private Map<String, List<String>> extractErrors(BindingResult bindingResult) {
        List<String> errors = bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return Map.of(JSON_KEY_ERRORS, errors);
    }
}
