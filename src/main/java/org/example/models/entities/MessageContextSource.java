package org.example.models.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "message_context_sources")
public class MessageContextSource extends BaseEntity{
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;

    // Резултатът от сравнението (напр. 0.85 = 85% съвпадение)
    @Column(name = "similarity_score", nullable = false)
    private Double score;

    public MessageContextSource(Message message, DocumentChunk chunk, Double score) {
        this.message = message;
        this.chunk = chunk;
        this.score = score;
    }

    public MessageContextSource() {
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public DocumentChunk getChunk() {
        return chunk;
    }

    public void setChunk(DocumentChunk chunk) {
        this.chunk = chunk;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
