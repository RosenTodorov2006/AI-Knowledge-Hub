package org.example.models.dtos.exportDtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageResponseDto {
    private String content;
    private String role;
    private LocalDateTime createdAt;
    private List<String> sources;

    public MessageResponseDto(String content, String role, LocalDateTime createdAt, List<String> sources) {
        this.content = content;
        this.role = role;
        this.createdAt = createdAt;
        this.sources = sources != null ? sources : new ArrayList<>();
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

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

}
