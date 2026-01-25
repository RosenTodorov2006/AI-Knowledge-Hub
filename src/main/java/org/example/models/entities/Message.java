package org.example.models.entities;

import jakarta.persistence.*;
import org.example.models.entities.enums.MessageRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
public class Message extends BaseEntity{
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageRole role;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageContextSource> contextSources = new ArrayList<>();

    public Message(MessageRole role, String content, LocalDateTime createdAt, Chat chat, List<MessageContextSource> contextSources) {
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
        this.chat = chat;
        this.contextSources = contextSources;
    }

    public Message() {
    }

    public MessageRole getRole() {
        return role;
    }

    public void setRole(MessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public List<MessageContextSource> getContextSources() {
        return contextSources;
    }

    public void setContextSources(List<MessageContextSource> contextSources) {
        this.contextSources = contextSources;
    }
}
