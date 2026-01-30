package org.example.services;

import org.example.models.entities.Document;
import org.example.models.entities.ProcessingJob;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProcessingJobService {
    ProcessingJob findByDocumentId(long documentId);
    void saveProcessingJob (ProcessingJob processingJob);
    void createProcessingJob(Document document);
    List<ProcessingJob> findAllFailedJobs();
    long countCompletedJobs();
    long countRunningJobs();
    long countFailedJobs();
}
