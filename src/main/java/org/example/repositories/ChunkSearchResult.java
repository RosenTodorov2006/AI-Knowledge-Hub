package org.example.repositories;

import org.example.models.entities.DocumentChunk;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkSearchResult {
    Long getId();         // Трябва ни за връзката в базата
    String getContent();  // Трябва ни за контекста на AI
    Double getSimilarity();
}
