package org.example.controllers.rest;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.dtos.importDtos.ChatRequestDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@RestController
@RequestMapping("/api/chats")
public class ChatRestController {
    private final DashboardService dashboardService;
    private final ChatService chatService;
    private final UserService userService;
    private final MessageSource messageSource;

    public ChatRestController(DashboardService dashboardService,
                              ChatService chatService,
                              UserService userService,
                              MessageSource messageSource) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(Principal principal) {
        return ResponseEntity.ok(assembleDashboardData(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatViewDto> getChatDetails(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(chatService.getChatDetails(id, principal.getName()));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestParam("file") MultipartFile file,
                                        Principal principal,
                                        Locale locale) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", messageSource.getMessage("error.chat.file.empty", null, locale)));
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(newChatDto);
        } catch (Exception e) {
            String prefix = messageSource.getMessage("error.chat.process.prefix", null, locale);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", prefix + " " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> sendMessage(@PathVariable Long id,
                                         @RequestParam("message") String content,
                                         Locale locale) {
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(chatService.generateResponse(id, content));
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("error.chat.send.failed", null, locale);
            return ResponseEntity.internalServerError().body(Map.of("error", errorMsg));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id, Principal principal, Locale locale) {
        try {
            chatService.deleteChat(id, principal.getName());
            return ResponseEntity.ok(Map.of("success", "Chat deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Could not delete chat."));
        }
    }

    private Map<String, Object> assembleDashboardData(String email) {
        return Map.of(
                "allChats", dashboardService.getAllChats(email),
                "currentUser", userService.getUserViewByEmail(email)
        );
    }
}
