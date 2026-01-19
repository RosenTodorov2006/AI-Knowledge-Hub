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
import org.example.services.MessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageContextSourceRepository messageContextSourceRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public MessageServiceImpl(MessageRepository messageRepository,
                              MessageContextSourceRepository messageContextSourceRepository, DocumentChunkRepository documentChunkRepository) {
        this.messageRepository = messageRepository;
        this.messageContextSourceRepository = messageContextSourceRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Override
    @Transactional
    public Message saveMessage(Chat chat, String content, MessageRole role) {
        Message message = new Message();
        message.setChat(chat);
        message.setContent(content);
        message.setRole(role);
        message.setCreatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void saveMessageSources(Message message, List<ChunkSearchResult> searchResults) {
        for (ChunkSearchResult res : searchResults) {
            MessageContextSource source = new MessageContextSource();
            source.setMessage(message);

            // Вземаме референция към чанка чрез неговото ID
            source.setChunk(documentChunkRepository.getReferenceById(res.getId()));

            source.setScore(res.getSimilarity());
            messageContextSourceRepository.save(source);
        }
    }
}
