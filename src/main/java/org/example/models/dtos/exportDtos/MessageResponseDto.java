package org.example.models.dtos.exportDtos;

import java.time.LocalDateTime;

public class MessageResponseDto {
    private String content;
    private String role;
    private LocalDateTime createdAt;

    public MessageResponseDto(String content, String role, LocalDateTime createdAt) {
        this.content = content;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
