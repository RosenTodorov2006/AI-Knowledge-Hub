package org.example.services.impl;

import org.example.models.entities.Message;
import org.example.models.entities.MessageContextSource;
import org.example.repositories.ChunkSearchResult;
import org.example.repositories.MessageContextSourceRepository;
import org.example.services.DocumentProcessingService;
import org.example.services.MessageContextSourceService;
import org.springframework.stereotype.Service;

@Service
public class MessageContextSourceServiceImpl implements MessageContextSourceService {
    private final MessageContextSourceRepository messageContextSourceRepository;
    private final DocumentProcessingService documentProcessingService;

    public MessageContextSourceServiceImpl(MessageContextSourceRepository messageContextSourceRepository, DocumentProcessingService documentProcessingService) {
        this.messageContextSourceRepository = messageContextSourceRepository;
        this.documentProcessingService = documentProcessingService;
    }
    @Override
    public void saveSource(Message message, ChunkSearchResult result) {
        MessageContextSource source = new MessageContextSource();
        source.setMessage(message);
        source.setChunk(documentProcessingService.getChunkById(result.getId()));
        source.setScore(result.getSimilarity());
        messageContextSourceRepository.save(source);
    }
}
