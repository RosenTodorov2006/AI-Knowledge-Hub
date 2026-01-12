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
import org.example.services.DashboardService;
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
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public DashboardServiceImpl(ChatRepository chatRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ChatDto> getAllChats(String gmail) {
        Optional<UserEntity> optionalUser = this.userRepository.findByEmail(gmail);
        if(optionalUser.isEmpty()){
            throw new NullPointerException("USER NOT FOUND!");
        }
        UserEntity userEntity = optionalUser.get();
        List<Chat> currentChats = chatRepository.findAllByUserEntityId(userEntity.getId());
        List<ChatDto> chatDtoList = new ArrayList<>();
        for (Chat chat : currentChats){
            ChatDto mappedChatDto = this.modelMapper.map(chat, ChatDto.class);
            mappedChatDto.setFilename(chat.getDocument().getFilename());
            mappedChatDto.setDocumentStatus(chat.getDocument().getStatus());
            mappedChatDto.setUploadedAt(chat.getDocument().getUploadedAt());
            chatDtoList.add(mappedChatDto);
        }
        return chatDtoList;
    }
}
