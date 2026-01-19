package org.example.repositories;

import org.example.models.entities.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    @Query(value = """
            SELECT c.*, (1 - (c.embedding <=> cast(:queryVector as vector))) as similarity 
            FROM document_chunks c
            WHERE c.document_id = :documentId 
            ORDER BY similarity DESC 
            LIMIT :limit
            """, nativeQuery = true)
    List<ChunkSearchResult> findTopSimilar(@Param("documentId") Long documentId,
                                           @Param("queryVector") float[] queryVector,
                                           @Param("limit") int limit);
}
