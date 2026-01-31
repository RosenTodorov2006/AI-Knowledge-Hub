package org.example.controllers.rest;

import jakarta.validation.Valid;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private static final String MSG_KEY_PW_UPDATED = "settings.success.password";
    private static final String MSG_KEY_DELETED = "settings.success.deleted";

    private final UserService userService;
    private final MessageSource messageSource;

    public SettingsRestController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
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
            String msg = messageSource.getMessage(MSG_KEY_PW_UPDATED, null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(JSON_KEY_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> delete(Principal principal) {
        try {
            userService.deleteUser(principal.getName());
            String msg = messageSource.getMessage(MSG_KEY_DELETED, null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(Map.of(JSON_KEY_MESSAGE, msg));
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
