package org.example.controllers.web;

import jakarta.servlet.http.HttpSession;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.example.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ChatController {
    public static final String SESSION_CHAT_ID = "currentChatId";
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

    public ChatController(DashboardService dashboardService, ChatService chatService, UserService userService) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName();
        model.addAttribute(ATTR_ALL_CHATS, dashboardService.getAllChats(email));
        model.addAttribute(ATTR_CURRENT_USER, userService.getUserViewByEmail(email));
        return "dashboard";
    }
    @GetMapping("/chats/select/{id}")
    public String selectChat(@PathVariable Long id, HttpSession session) {
        session.setAttribute(SESSION_CHAT_ID, id);
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String viewChat(HttpSession session, Model model, Principal principal) {
        Long id = (Long) session.getAttribute(SESSION_CHAT_ID);

        if (id == null) {
            return "redirect:/dashboard";
        }

        model.addAttribute(ATTR_CHAT, chatService.getChatDetails(id, principal.getName()));
        return "chat";
    }

    @PostMapping("/chats/create")
    public String processCreateChat(@RequestParam("file") MultipartFile file,
                                    Principal principal,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, ERR_MSG_EMPTY_FILE);
            return "redirect:/dashboard";
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            session.setAttribute(SESSION_CHAT_ID, newChatDto.getId());
            return "redirect:/chat";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, ERR_MSG_PROCESS_PREFIX + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/chat/send")
    public String sendMessage(HttpSession session,
                              @RequestParam("message") String content) {
        Long id = (Long) session.getAttribute(SESSION_CHAT_ID);

        if (id == null || content == null || content.trim().isEmpty()) {
            return "redirect:/chat";
        }

        try {
            chatService.generateResponse(id, content);
            return "redirect:/chat" + ANCHOR_BOTTOM;
        } catch (Exception e) {
            return "redirect:/chat" + "?" + PARAM_ERROR_TRUE;
        }
    }
}
