package org.example.services.impl;

import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.example.models.dtos.exportDtos.ProcessingJobDto;
import org.example.models.entities.Document;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.enums.DocumentStatus;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.ChatRepository;
import org.example.repositories.DocumentChunkRepository;
import org.example.repositories.DocumentRepository;
import org.example.repositories.ProcessingJobRepository;
import org.example.services.AdminService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final ProcessingJobRepository processingJobRepository;
    private final ChatRepository chatRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public AdminServiceImpl(ProcessingJobRepository processingJobRepository,
                            ChatRepository chatRepository, DocumentChunkRepository documentChunkRepository) {
        this.processingJobRepository = processingJobRepository;
        this.chatRepository = chatRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Override
    public List<ProcessingJobDto> getFailedJobs() {
        List<ProcessingJob> failedJobs = processingJobRepository.findAllByStage(ProcessingJobStage.FAILED);

        return failedJobs.stream().map(job -> {
            ProcessingJobDto dto = new ProcessingJobDto();
            dto.setJobId("#JOB-" + job.getId());
            dto.setFileName(job.getDocument().getFilename());

            String email = chatRepository.findByDocument(job.getDocument())
                    .map(chat -> chat.getUserEntity().getEmail())
                    .orElse("System / Orphaned");
            dto.setUserEmail(email);

            dto.setError(job.getErrorMessage());

            dto.setTimeAgo(formatTimeAgo(job.getDocument().getUploadedAt()));

            ProcessingJobStage currentStage = job.getStage();

            dto.setExtractPassed(currentStage.ordinal() > ProcessingJobStage.PARSING.ordinal());

            dto.setChunkPassed(currentStage.ordinal() > ProcessingJobStage.SPLIT.ordinal());

            dto.setEmbedPassed(currentStage == ProcessingJobStage.INDEXING
                    || currentStage == ProcessingJobStage.COMPLETED);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public AdminStatsDto getSystemStats() {
        long processing = processingJobRepository.countByStageNotAndErrorMessageIsNull(ProcessingJobStage.COMPLETED);

        long completed = processingJobRepository.countByStage(ProcessingJobStage.COMPLETED);
        long failed = processingJobRepository.countByErrorMessageIsNotNull();
        double rate = (completed + failed == 0) ? 100.0 : ((double) completed / (completed + failed)) * 100;

        long vectorsCount = documentChunkRepository.count();
        String formattedVectors = formatVectorCount(vectorsCount);

        return new AdminStatsDto(
                processing,
                Math.round(rate * 10.0) / 10.0,
                formattedVectors
        );
    }

    private String formatVectorCount(long count) {
        if (count < 1000) return String.valueOf(count);
        if (count < 1_000_000) return String.format("%.1fK", count / 1000.0);
        return String.format("%.1fM", count / 1_000_000.0);
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "n/a";
        Duration d = Duration.between(dateTime, LocalDateTime.now());
        if (d.getSeconds() < 60) return d.getSeconds() + "s ago";
        if (d.toMinutes() < 60) return d.toMinutes() + "m ago";
        if (d.toHours() < 24) return d.toHours() + "h ago";
        return d.toDays() + "d ago";
    }
}