package org.example.models.entities;

import jakarta.persistence.*;
import org.example.models.entities.BaseEntity;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.models.entities.enums.ProcessingJobStatus;

@Entity
@Table(name = "processing_jobs")
public class ProcessingJob extends BaseEntity {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessingJobStage stage;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessingJobStatus status;
    @Column(name = "error_message")
    private String errorMessage;
    @OneToOne
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    public ProcessingJob(ProcessingJobStage stage, ProcessingJobStatus status, String errorMessage, Document document) {
        this.stage = stage;
        this.status = status;
        this.errorMessage = errorMessage;
        this.document = document;
    }

    public ProcessingJob() {
    }

    public ProcessingJobStage getStage() {
        return stage;
    }

    public void setStage(ProcessingJobStage stage) {
        this.stage = stage;
    }

    public ProcessingJobStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingJobStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
