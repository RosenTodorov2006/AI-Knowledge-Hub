package org.example.controllers.rest;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.dtos.importDtos.ChatRequestDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
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

import static org.example.controllers.rest.UserRestController.JSON_KEY_ERROR;

@RestController
@RequestMapping("/api/chats")
public class ChatRestController {
    private static final String MSG_KEY_EMPTY_FILE = "error.chat.file.empty";
    private static final String MSG_KEY_PROCESS_PREFIX = "error.chat.process.prefix";
    public static final String ERR_MSG_PROCESS_PREFIX = "Processing error: ";
    private final DashboardService dashboardService;
    private final ChatService chatService;
    private final MessageSource messageSource;

    public ChatRestController(DashboardService dashboardService, ChatService chatService, MessageSource messageSource) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public List<ChatDto> getDashboardData(Principal principal) {
        return this.dashboardService.getAllChats(principal.getName());
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createChat(@RequestParam("file") MultipartFile file,
                                             Principal principal) {
        Locale locale = LocaleContextHolder.getLocale();

        if (file.isEmpty()) {
            String errorMsg = messageSource.getMessage(MSG_KEY_EMPTY_FILE, null, locale);
            return ResponseEntity.badRequest()
                    .body(Map.of(JSON_KEY_ERROR, errorMsg));
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(newChatDto);
        } catch (Exception e) {
            String prefix = messageSource.getMessage(MSG_KEY_PROCESS_PREFIX, null, locale);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(JSON_KEY_ERROR, prefix + " " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ChatResponseDto> sendMessage(
            @PathVariable Long id,
            @RequestBody ChatRequestDto requestDto) {

        ChatResponseDto response = chatService.generateResponse(id, requestDto.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatViewDto> getChatDetails(@PathVariable Long id, Principal principal) {
        ChatViewDto chatDetails = chatService.getChatDetails(id, principal.getName());
        return ResponseEntity.ok(chatDetails);
    }
}
