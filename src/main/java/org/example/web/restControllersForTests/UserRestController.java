package org.example.web.restControllersForTests;

import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test/auth")
public class UserRestController {
    private final UserService userService;
    public UserRestController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> testRegister(@RequestBody @Valid RegisterSeedDto registerSeedDto,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Грешка: Потребителят вече съществува!"));
        }
        try {
            this.userService.register(registerSeedDto);
            return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("No user is currently logged in.");
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }
}
