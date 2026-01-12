package org.example.web;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
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

    public DashboardController(DashboardService dashboardService, ChatService chatService) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        List<ChatDto> allChats = this.dashboardService.getAllChats(principal.getName());
        model.addAttribute("allChats", allChats);
        return "dashboard";
    }

    @GetMapping("/{id}")
    public String viewChat(@PathVariable Long id, Model model) {
        // retrieving current chat from the database via the service
        return "chat-split-view";
    }
    @GetMapping("/create")
    public String createChat(Model model) {
        // service logic
        return "chat-create";
    }
    @PostMapping("/create")
    public String processCreateChat(@RequestParam("file") MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file.");
            return "redirect:/chats/create";
        }
        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Chat successfully started for: " + newChatDto.getDocumentFilename());
            return "redirect:/chats/" + newChatDto.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Processing error: " + e.getMessage());
            return "redirect:/chats/create";
        }
    }
    @PostMapping("/{id}/send")
    public String sendMessage(@PathVariable Long id,
                              @RequestParam("message") String content) {

        if (content == null || content.trim().isEmpty()) {
            return "redirect:/chats/" + id;
        }
        // sending the question to openAI along with the vector and adding the answer and question to the service's message list
        return "redirect:/chats/" + id;
    }
}
