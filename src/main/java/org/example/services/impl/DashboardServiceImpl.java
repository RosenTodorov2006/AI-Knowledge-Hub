package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.example.models.dtos.exportDtos.ChatDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.entities.Chat;
import org.example.models.entities.Document;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.DocumentStatus;
import org.example.repositories.ChatRepository;
import org.example.repositories.DocumentRepository;
import org.example.repositories.UserRepository;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.example.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final ChatService chatService;

    public DashboardServiceImpl(ModelMapper modelMapper, UserService userService, ChatService chatService) {
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.chatService = chatService;
    }

    @Override
    public List<ChatDto> getAllChats(String gmail) {
        UserEntity userEntity = this.userService.findUserByEmail(gmail);
        List<Chat> currentChats = chatService.findAllChatsByUserEntityId(userEntity.getId());
        List<ChatDto> chatDtoList = new ArrayList<>();
        for (Chat chat : currentChats){
            ChatDto mappedChatDto = this.modelMapper.map(chat, ChatDto.class);
            mappedChatDto.setFilename(chat.getDocument().getFilename());
            mappedChatDto.setDocumentStatus(chat.getDocument().getDocumentStatus());
            mappedChatDto.setUploadedAt(chat.getDocument().getUploadedAt());
            chatDtoList.add(mappedChatDto);
        }
        chatDtoList.sort((c1, c2) -> c2.getUploadedAt().compareTo(c1.getUploadedAt()));
        return chatDtoList;
    }
}
