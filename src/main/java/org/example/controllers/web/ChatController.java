package org.example.controllers.web;

import jakarta.servlet.http.HttpSession;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Locale;

@Controller
public class ChatController {
    public static final String SESSION_CHAT_ID = "currentChatId";
    public static final String ATTR_ALL_CHATS = "allChats";
    public static final String ATTR_CURRENT_USER = "currentUser";
    public static final String ATTR_CHAT = "chat";
    public static final String ATTR_ERROR = "error";
    public static final String ANCHOR_BOTTOM = "#bottom-anchor";
    private static final String MSG_ERR_FILE_EMPTY = "error.chat.file.empty";
    private static final String MSG_ERR_PROCESS_PREFIX = "error.chat.process.prefix";
    private static final String MSG_ERR_SEND_FAILED = "error.chat.send.failed";

    private final DashboardService dashboardService;
    private final ChatService chatService;
    private final UserService userService;
    private final MessageSource messageSource;

    public ChatController(DashboardService dashboardService,
                          ChatService chatService,
                          UserService userService,
                          MessageSource messageSource) {
        this.dashboardService = dashboardService;
        this.chatService = chatService;
        this.userService = userService;
        this.messageSource = messageSource;
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
        Locale locale = LocaleContextHolder.getLocale();

        if (file.isEmpty()) {
            String errorMsg = messageSource.getMessage(MSG_ERR_FILE_EMPTY, null, locale);
            redirectAttributes.addFlashAttribute(ATTR_ERROR, errorMsg);
            return "redirect:/dashboard";
        }

        try {
            ChatViewDto newChatDto = chatService.startNewChat(file, principal.getName());
            session.setAttribute(SESSION_CHAT_ID, newChatDto.getId());
            return "redirect:/chat";
        } catch (Exception e) {
            String prefix = messageSource.getMessage(MSG_ERR_PROCESS_PREFIX, null, locale);
            redirectAttributes.addFlashAttribute(ATTR_ERROR, prefix + " " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/chat/send")
    public String sendMessage(HttpSession session,
                              @RequestParam("message") String content,
                              RedirectAttributes redirectAttributes) {
        Long id = (Long) session.getAttribute(SESSION_CHAT_ID);
        Locale locale = LocaleContextHolder.getLocale();

        if (id == null || content == null || content.trim().isEmpty()) {
            return "redirect:/chat";
        }

        try {
            chatService.generateResponse(id, content);
            return "redirect:/chat" + ANCHOR_BOTTOM;
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage(MSG_ERR_SEND_FAILED, null, locale);
            redirectAttributes.addFlashAttribute(ATTR_ERROR, errorMsg);
            return "redirect:/chat";
        }
    }
}
