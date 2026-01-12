package org.example.services;

import org.example.models.dtos.exportDtos.ChatViewDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ChatService {
    ChatViewDto startNewChat(MultipartFile file, String name) throws IOException;
}
