package org.example.models.entities;

import jakarta.persistence.*;
import org.example.models.entities.enums.DocumentStatus;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document extends BaseEntity {
    @Column(nullable = false)
    private String filename;
    @Column(nullable = false, name = "mime_type")
    private String mimeType;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus;
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    @Column(name = "content", nullable = false)
    @JdbcType(VarbinaryJdbcType.class)
    private byte[] content;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProcessingJob processingJob;

    public Document() {
    }

    public Document(String filename, String mimeType, DocumentStatus documentStatus, LocalDateTime uploadedAt, byte[] content) {
        this.filename = filename;
        this.mimeType = mimeType;
        this.documentStatus = documentStatus;
        this.uploadedAt = uploadedAt;
        this.content = content;
    }

    public ProcessingJob getProcessingJob() {
        return processingJob;
    }

    public void setProcessingJob(ProcessingJob processingJob) {
        this.processingJob = processingJob;
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

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}