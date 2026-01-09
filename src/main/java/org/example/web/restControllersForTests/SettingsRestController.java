package org.example.web.restControllersForTests;

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
    private final UserService userService;
    public SettingsRestController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/changeInfo")
    public ResponseEntity<?> changeInfo(@RequestBody @Valid ChangeProfileDto changeProfileDto, BindingResult bindingResult, Principal principal){
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(getErrors(bindingResult));
        }
        this.userService.changeProfileInfo(changeProfileDto, principal.getName());
        UserViewDto updatedData = this.userService.getUserViewByEmail(changeProfileDto.getEmail());
        return ResponseEntity.ok(updatedData);
    }
    @PostMapping("/changeUserPassword")
    public ResponseEntity<?> changeUserPassword(@RequestBody @Valid ChangeUserPasswordDto changeUserPasswordDto, BindingResult bindingResult, Principal principal){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(getErrors(bindingResult));
        }
        try {
            userService.changeUserPassword(changeUserPasswordDto, principal.getName());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully! Please use your new password for the next login."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping()
    public ResponseEntity<?> delete(Principal principal){
        try{
            userService.deleteUser(principal.getName());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully!"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    private Map<String, List<String>> getErrors(BindingResult bindingResult) {
        List<String> errors = bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return Map.of("errors", errors);
    }
}
