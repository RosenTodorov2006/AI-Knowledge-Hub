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
import org.example.services.ChatService;
import org.example.services.DocumentProcessingService;
import org.example.services.ProcessingJobService;
import org.example.utils.DateTimeUtils;
import org.example.utils.FormatUtils;
import org.example.utils.JobUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    public static final String JOB_ID_PREFIX = "#JOB-";

    private final ModelMapper modelMapper;
    private final ProcessingJobService processingJobService;
    private final DocumentProcessingService documentProcessingService;
    private final ChatService chatService;

    public AdminServiceImpl(ModelMapper modelMapper,
                            ProcessingJobService processingJobService,
                            DocumentProcessingService documentProcessingService,
                            ChatService chatService) {
        this.modelMapper = modelMapper;
        this.processingJobService = processingJobService;
        this.documentProcessingService = documentProcessingService;
        this.chatService = chatService;
    }

    @Override
    public List<ProcessingJobDto> getFailedJobs() {
        return processingJobService.findAllFailedJobs()
                .stream()
                .map(this::convertToFailedJobDto)
                .collect(Collectors.toList());
    }

    @Override
    public AdminStatsDto getSystemStats() {
        long processing = processingJobService.countRunningJobs();
        long completed = processingJobService.countCompletedJobs();
        long failed = processingJobService.countFailedJobs();

        double successRate = FormatUtils.calculateSuccessRate(completed, failed);
        String formattedVectors = FormatUtils.formatVectorCount(documentProcessingService.getTotalVectorCount());

        return new AdminStatsDto(processing, successRate, formattedVectors);
    }

    private ProcessingJobDto convertToFailedJobDto(ProcessingJob job) {
        ProcessingJobDto dto = modelMapper.map(job, ProcessingJobDto.class);

        dto.setJobId(JOB_ID_PREFIX + job.getId());
        dto.setFileName(job.getDocument().getFilename());
        dto.setUserEmail(chatService.findUserEmailByDocument(job.getDocument()));

        dto.setTimeAgo(DateTimeUtils.formatTimeAgo(job.getDocument().getUploadedAt()));

        JobUtils.enrichStageFlags(dto, job.getStage());

        return dto;
    }
}