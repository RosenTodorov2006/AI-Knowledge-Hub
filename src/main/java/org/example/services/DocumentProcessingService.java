package org.example.services;

import org.example.models.entities.DocumentChunk;
import org.example.repositories.ChunkSearchResult;

import java.util.List;

public interface DocumentProcessingService {
    void processDocument(Long documentId);
    DocumentChunk getChunkById(long id);
    List<ChunkSearchResult> findTopSimilar(long documentId, float[] queryVector, int defaultTopK);
    long getTotalVectorCount();

}
