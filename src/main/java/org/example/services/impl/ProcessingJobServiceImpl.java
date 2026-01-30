package org.example.services.impl;

import org.example.models.entities.Document;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.ProcessingJobRepository;
import org.example.services.ProcessingJobService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessingJobServiceImpl implements ProcessingJobService {
    private static final String ERR_JOB_NOT_FOUND = "Job not found";
    private final ProcessingJobRepository processingJobRepository;

    public ProcessingJobServiceImpl(ProcessingJobRepository processingJobRepository) {
        this.processingJobRepository = processingJobRepository;
    }

    @Override
    public ProcessingJob findByDocumentId(long documentId) {
        return processingJobRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException(ERR_JOB_NOT_FOUND));
    }

    @Override
    public void saveProcessingJob(ProcessingJob processingJob) {
        processingJobRepository.save(processingJob);
    }
    @Override
    public void createProcessingJob(Document document) {
        ProcessingJob job = new ProcessingJob();
        job.setDocument(document);
        job.setStage(ProcessingJobStage.UPLOADED);
        processingJobRepository.save(job);
    }
    @Override
    public List<ProcessingJob> findAllFailedJobs() {
        return processingJobRepository.findAllByStage(ProcessingJobStage.FAILED);
    }

    @Override
    public long countCompletedJobs() {
        return processingJobRepository.countByStage(ProcessingJobStage.COMPLETED);
    }

    @Override
    public long countRunningJobs() {
        return processingJobRepository.countByStageNotAndErrorMessageIsNull(ProcessingJobStage.COMPLETED);
    }

    @Override
    public long countFailedJobs() {
        return processingJobRepository.countByErrorMessageIsNotNull();
    }
}
