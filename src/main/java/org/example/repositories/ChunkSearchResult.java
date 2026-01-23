package org.example.repositories;

import org.example.models.entities.DocumentChunk;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkSearchResult {
    Long getId();
    String getContent();
    Double getSimilarity();
}
