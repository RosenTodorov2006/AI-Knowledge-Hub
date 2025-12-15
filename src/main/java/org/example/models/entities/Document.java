package org.example.models.entities;

import jakarta.persistence.*;
import org.example.models.entities.enums.DocumentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document extends BaseEntity{
    @Column(nullable = false)
    private String filename;
    @Column(nullable = false, name = "mime_type")
    private String mimeType;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus;
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Document(String filename, String mimeType, DocumentStatus documentStatus, LocalDateTime uploadedAt, User user) {
        this.filename = filename;
        this.mimeType = mimeType;
        this.documentStatus = documentStatus;
        this.uploadedAt = uploadedAt;
        this.user = user;
    }

    public Document() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public DocumentStatus getStatus() {
        return documentStatus;
    }

    public void setStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
