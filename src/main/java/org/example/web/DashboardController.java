package org.example.web;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.example.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {

    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_CHAT = "chat";
    public static final String REDIRECT_DASHBOARD = "redirect:/dashboard";
    public static final String REDIRECT_CHAT_PREFIX = "redirect:/chats/";
    public static final String ATTR_ALL_CHATS = "allChats";
    public static final String ATTR_CURRENT_USER = "currentUser";
    public static final String ATTR_CHAT = "chat";
    public static final String ATTR_ERROR = "error";
    public static final String PARAM_ERROR_TRUE = "error=true";
    public static final String ANCHOR_BOTTOM = "#bottom-anchor";
    public static final String ERR_MSG_EMPTY_FILE = "Please select a file.";
    public static final String ERR_MSG_PROCESS_PREFIX = "Processing error: ";

    private final DashboardService dashboardService;
    private final ChatService chatService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, ChatService chatService, UserService userService) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName();

        model.addAttribute(ATTR_ALL_CHATS, dashboardService.getAllChats(email));
        model.addAttribute(ATTR_CURRENT_USER, userService.getUserViewByEmail(email));

        return VIEW_DASHBOARD;
    }

    @GetMapping("/chats/{id}")
    public String viewChat(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute(ATTR_CHAT, chatService.getChatDetails(id, principal.getName()));
        return VIEW_CHAT;
    }

    @PostMapping("/chats/create")
    public String processCreateChat(@RequestParam("file") MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, ERR_MSG_EMPTY_FILE);
            return REDIRECT_DASHBOARD;
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            return REDIRECT_CHAT_PREFIX + newChatDto.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, ERR_MSG_PROCESS_PREFIX + e.getMessage());
            return REDIRECT_DASHBOARD;
        }
    }

    @PostMapping("/chats/{id}/send")
    public String sendMessage(@PathVariable Long id,
                              @RequestParam("message") String content) {
        if (content == null || content.trim().isEmpty()) {
            return REDIRECT_CHAT_PREFIX + id;
        }

        try {
            chatService.generateResponse(id, content);
            return REDIRECT_CHAT_PREFIX + id + ANCHOR_BOTTOM;
        } catch (Exception e) {
            return REDIRECT_CHAT_PREFIX + id + "?" + PARAM_ERROR_TRUE;
        }
    }
}
