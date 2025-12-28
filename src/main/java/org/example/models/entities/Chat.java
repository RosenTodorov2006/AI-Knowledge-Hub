package org.example.models.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat extends BaseEntity{
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, name = "last_message_at")
    private LocalDateTime lastMessageAt;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages;
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    public Chat(String title, LocalDateTime lastMessageAt, User user, List<Message> messages, Document document) {
        this.title = title;
        this.lastMessageAt = lastMessageAt;
        this.user = user;
        this.messages = messages;
        this.document = document;
    }

    public Chat() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
