package org.example.web.restControllersForTests;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats") // Изчистен и кратък път
public class DashboardRestController {
    private final DashboardService dashboardService;
    private final ChatService chatService;

    public DashboardRestController(DashboardService dashboardService, ChatService chatService) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatDto> getDashboardData(Principal principal) {
        return this.dashboardService.getAllChats(principal.getName());
    }
    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestParam("file") MultipartFile file,
                                        Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a file."));
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(newChatDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Processing error: " + e.getMessage()));
        }
    }
}
