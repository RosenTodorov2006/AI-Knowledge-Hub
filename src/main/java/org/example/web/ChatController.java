package org.example.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/chats")
public class ChatController {
    @GetMapping
    public String allChats(Model model) {
        // retrieving all chats from the database via the service
        return "chats";
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
                                    @RequestParam("title") String title,
                                    RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/chats/create";
        }
        // service logic
        return "redirect:/chats/"; //+newChatId
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
