package org.example.services;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DashboardService {
    List<ChatDto> getAllChats(String gmail);

    ChatViewDto startNewChat(MultipartFile file, String name) throws IOException;
}
