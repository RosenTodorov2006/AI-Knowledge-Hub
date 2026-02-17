package org.example.services;

import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.entities.Chat;
import org.example.models.entities.Document;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ChatService {
    ChatViewDto startNewChat(MultipartFile file, String name) throws IOException;
    ChatResponseDto generateResponse(Long chatId, String content);
    ChatViewDto getChatDetails(Long id, String gmail);
    List<Chat> findAllChatsByUserEntityId(long userId);
    String findUserEmailByDocument(Document document);
    void deleteChat(Long chatId, String userEmail);
}
