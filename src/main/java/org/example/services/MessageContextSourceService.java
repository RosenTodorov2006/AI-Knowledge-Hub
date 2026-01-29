package org.example.services;

import org.example.models.entities.Message;
import org.example.models.entities.MessageContextSource;
import org.example.repositories.ChunkSearchResult;

public interface MessageContextSourceService {
    void saveSource(Message message, ChunkSearchResult result);
}
