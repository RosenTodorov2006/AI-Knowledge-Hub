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
    private UserEntity userEntity;
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages;
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "openai_thread_id")
    private String openAiThreadId;

    public Chat(String title, LocalDateTime lastMessageAt, UserEntity userEntity, List<Message> messages, Document document, String openAiThreadId) {
        this.title = title;
        this.lastMessageAt = lastMessageAt;
        this.userEntity = userEntity;
        this.messages = messages;
        this.document = document;
        this.openAiThreadId = openAiThreadId;
    }
    public Chat(String title, UserEntity userEntity, Document document, LocalDateTime lastMessageAt) {
        this.title = title;
        this.userEntity = userEntity;
        this.document = document;
        this.lastMessageAt = lastMessageAt;
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

    public UserEntity getUser() {
        return userEntity;
    }

    public void setUser(UserEntity userEntity) {
        this.userEntity = userEntity;
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

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public String getOpenAiThreadId() {
        return openAiThreadId;
    }

    public void setOpenAiThreadId(String openAiThreadId) {
        this.openAiThreadId = openAiThreadId;
    }
}
