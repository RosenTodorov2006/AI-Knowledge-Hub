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
    private final DashboardService dashboardService;
    private final ChatService chatService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, ChatService chatService, UserService userService) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        List<ChatDto> allChats = this.dashboardService.getAllChats(principal.getName());
        model.addAttribute("allChats", allChats);
        UserViewDto currentUser = this.userService.getUserViewByEmail(principal.getName());
        model.addAttribute("currentUser", currentUser);
        return "dashboard";
    }

    @GetMapping("/chats/{id}")
    public String viewChat(@PathVariable Long id, Model model, Principal principal) {
        ChatViewDto chat = chatService.getChatDetails(id, principal.getName());

        model.addAttribute("chat", chat);
        return "chat";
    }

    @PostMapping("/chats/create")
    public String processCreateChat(@RequestParam("file") MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file.");
            return "redirect:/dashboard";
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());

            return "redirect:/chats/" + newChatDto.getId();

        } catch (Exception e) {
            System.err.println("Error creating chat: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Processing error: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }
    @PostMapping("/chats/{id}/send")
    public String sendMessage(@PathVariable Long id,
                              @RequestParam("message") String content) {
        if (content == null || content.trim().isEmpty()) {
            return "redirect:/chats/" + id;
        }

        try {
            chatService.generateResponse(id, content);

        } catch (Exception e) {
            System.err.println("Error generating AI response: " + e.getMessage());
            return "redirect:/chats/" + id + "?error=true";
        }

        return "redirect:/chats/" + id + "#bottom-anchor";
    }
}
