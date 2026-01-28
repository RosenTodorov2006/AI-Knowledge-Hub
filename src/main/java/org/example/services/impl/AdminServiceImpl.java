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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    public static final String JOB_ID_PREFIX = "#JOB-";
    public static final String DEFAULT_USER_EMAIL = "System / Orphaned";
    public static final String TIME_N_A = "n/a";
    private static final double MAX_PERCENTAGE = 100.0;
    private static final double PERCENTAGE_PRECISION = 10.0;
    private static final long THOUSAND = 1_000L;
    private static final long MILLION = 1_000_000L;
    private static final String FORMAT_KILO = "%.1fK";
    private static final String FORMAT_MEGA = "%.1fM";
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int HOURS_PER_DAY = 24;
    private static final String UNIT_SECONDS = "s ago";
    private static final String UNIT_MINUTES = "m ago";
    private static final String UNIT_HOURS = "h ago";
    private static final String UNIT_DAYS = "d ago";

    private final ProcessingJobRepository processingJobRepository;
    private final ChatRepository chatRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ModelMapper modelMapper;

    public AdminServiceImpl(ProcessingJobRepository processingJobRepository,
                            ChatRepository chatRepository,
                            DocumentChunkRepository documentChunkRepository,
                            ModelMapper modelMapper) {
        this.processingJobRepository = processingJobRepository;
        this.chatRepository = chatRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ProcessingJobDto> getFailedJobs() {
        return processingJobRepository.findAllByStage(ProcessingJobStage.FAILED)
                .stream()
                .map(this::convertToFailedJobDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminStatsDto getSystemStats() {
        long processing = processingJobRepository.countByStageNotAndErrorMessageIsNull(ProcessingJobStage.COMPLETED);
        long completed = processingJobRepository.countByStage(ProcessingJobStage.COMPLETED);
        long failed = processingJobRepository.countByErrorMessageIsNotNull();

        double successRate = calculateSuccessRate(completed, failed);
        String formattedVectors = formatVectorCount(documentChunkRepository.count());

        return new AdminStatsDto(processing, successRate, formattedVectors);
    }

    private ProcessingJobDto convertToFailedJobDto(ProcessingJob job) {
        ProcessingJobDto dto = modelMapper.map(job, ProcessingJobDto.class);

        dto.setJobId(JOB_ID_PREFIX + job.getId());
        dto.setFileName(job.getDocument().getFilename());
        dto.setUserEmail(findUserEmailByDocument(job.getDocument()));
        dto.setTimeAgo(formatTimeAgo(job.getDocument().getUploadedAt()));

        enrichStageFlags(dto, job.getStage());

        return dto;
    }

    private String findUserEmailByDocument(Document document) {
        return chatRepository.findByDocument(document)
                .map(chat -> chat.getUserEntity().getEmail())
                .orElse(DEFAULT_USER_EMAIL);
    }

    private void enrichStageFlags(ProcessingJobDto dto, ProcessingJobStage currentStage) {
        int currentOrdinal = currentStage.ordinal();
        dto.setExtractPassed(currentOrdinal > ProcessingJobStage.PARSING.ordinal());
        dto.setChunkPassed(currentOrdinal > ProcessingJobStage.SPLIT.ordinal());
        dto.setEmbedPassed(currentStage == ProcessingJobStage.INDEXING
                || currentStage == ProcessingJobStage.COMPLETED);
    }

    private double calculateSuccessRate(long completed, long failed) {
        if (completed + failed == 0) return MAX_PERCENTAGE;
        double rate = ((double) completed / (completed + failed)) * MAX_PERCENTAGE;
        return Math.round(rate * PERCENTAGE_PRECISION) / PERCENTAGE_PRECISION;
    }

    private String formatVectorCount(long count) {
        if (count < THOUSAND) return String.valueOf(count);
        if (count < MILLION) return String.format(FORMAT_KILO, count / (double) THOUSAND);
        return String.format(FORMAT_MEGA, count / (double) MILLION);
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return TIME_N_A;

        Duration d = Duration.between(dateTime, LocalDateTime.now());

        if (d.getSeconds() < SECONDS_PER_MINUTE) return d.getSeconds() + UNIT_SECONDS;
        if (d.toMinutes() < MINUTES_PER_HOUR) return d.toMinutes() + UNIT_MINUTES;
        if (d.toHours() < HOURS_PER_DAY) return d.toHours() + UNIT_HOURS;
        return d.toDays() + UNIT_DAYS;
    }
}