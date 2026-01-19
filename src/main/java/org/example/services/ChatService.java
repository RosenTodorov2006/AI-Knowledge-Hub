package org.example.services;

import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ChatService {
    ChatViewDto startNewChat(MultipartFile file, String name) throws IOException;
    ChatResponseDto generateResponse(Long chatId, String content);
}
