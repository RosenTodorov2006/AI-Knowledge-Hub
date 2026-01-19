package org.example.models.dtos.exportDtos;

import java.time.LocalDateTime;
import java.util.List;

public class ChatViewDto {
    private long id;
    private String title;
    private String documentFilename;
    private LocalDateTime lastMessageAt;
    private List<MessageResponseDto> messages;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDocumentFilename() {
        return documentFilename;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDocumentFilename(String documentFilename) {
        this.documentFilename = documentFilename;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<MessageResponseDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponseDto> messages) {
        this.messages = messages;
    }
}
