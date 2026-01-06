package org.example.models.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk extends BaseEntity{
    @Column(nullable = false, name = "chunk_index")
    private int chunkIndex;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector")
    private List<Double> embedding;
    @Column(nullable = false, name = "token_count")
    private int tokenCount;
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    public DocumentChunk(int chunkIndex, String content, List<Double> embedding, int tokenCount, Document document) {
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.embedding = embedding;
        this.tokenCount = tokenCount;
        this.document = document;
    }

    public DocumentChunk() {
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
