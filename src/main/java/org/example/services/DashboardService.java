package org.example.services;

import org.example.models.dtos.exportDtos.ChatDto;

import java.util.List;

public interface DashboardService {
    List<ChatDto> getAllChats(String gmail);
}
