package org.example.services;

import org.example.models.entities.Chat;
import org.example.models.entities.DocumentChunk;
import org.example.models.entities.Message;
import org.example.models.entities.enums.MessageRole;
import org.example.repositories.ChunkSearchResult;

import java.util.List;

public interface MessageService {
    Message saveMessage(Chat chat, String content, MessageRole role);
    void saveMessageSources(Message message, List<ChunkSearchResult> searchResults);
    void deleteMessagesByChatId(Long chatId);
}
