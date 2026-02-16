package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.example.models.entities.Chat;
import org.example.models.entities.DocumentChunk;
import org.example.models.entities.Message;
import org.example.models.entities.MessageContextSource;
import org.example.models.entities.enums.MessageRole;
import org.example.repositories.ChunkSearchResult;
import org.example.repositories.DocumentChunkRepository;
import org.example.repositories.MessageContextSourceRepository;
import org.example.repositories.MessageRepository;
import org.example.services.DocumentProcessingService;
import org.example.services.MessageContextSourceService;
import org.example.services.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageContextSourceService messageContextSourceService;

    public MessageServiceImpl(MessageRepository messageRepository, MessageContextSourceService messageContextSourceService) {
        this.messageRepository = messageRepository;
        this.messageContextSourceService = messageContextSourceService;
    }

    @Override
    @Transactional
    public Message saveMessage(Chat chat, String content, MessageRole role) {
        Message message = createMessage(chat, content, role);
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void saveMessageSources(Message message, List<ChunkSearchResult> searchResults) {
        searchResults.forEach(res -> messageContextSourceService.saveSource(message, res));
    }
    @Override
    @Transactional
    public void deleteMessagesByChatId(Long chatId) {
        messageRepository.deleteAllByChatId(chatId);
    }

    private Message createMessage(Chat chat, String content, MessageRole role) {
        Message message = new Message();
        message.setChat(chat);
        message.setContent(content);
        message.setRole(role);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
}
