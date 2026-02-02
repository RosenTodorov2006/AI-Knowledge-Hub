package org.example.unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.example.services.ProcessingJobService;
import org.example.services.DocumentProcessingService;
import org.example.services.ChatService;
import org.example.services.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.junit.jupiter.api.Assertions;
import org.example.models.entities.Document;
import java.time.LocalDateTime;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.models.dtos.exportDtos.ProcessingJobDto;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {
    private static final long RUNNING_JOBS = 5L;
    private static final long COMPLETED_JOBS = 95L;
    private static final long FAILED_JOBS = 5L;
    private static final long TOTAL_VECTOR_COUNT = 1000L;
    private static final double EXPECTED_SUCCESS_RATE = 95.0;
    private static final String EXPECTED_FORMATTED_VECTORS = "1,0K";
    private static final String TEST_FILENAME = "test.pdf";
    private static final String TEST_USER_EMAIL = "user@example.com";
    private static final long TEST_JOB_ID = 1L;
    private static final String EXPECTED_JOB_ID_DISPLAY = "#JOB-1";
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ProcessingJobService processingJobService;
    @Mock
    private DocumentProcessingService documentProcessingService;
    @Mock
    private ChatService chatService;
    private AdminServiceImpl adminService;
    @BeforeEach
    public void setUp() {
        adminService = new AdminServiceImpl(
                modelMapper,
                processingJobService,
                documentProcessingService,
                chatService
        );
    }
    @Test
    public void testGetSystemStatsShouldReturnCorrectData() {
        Mockito.when(processingJobService.countRunningJobs()).thenReturn(RUNNING_JOBS);
        Mockito.when(processingJobService.countCompletedJobs()).thenReturn(COMPLETED_JOBS);
        Mockito.when(processingJobService.countFailedJobs()).thenReturn(FAILED_JOBS);
        Mockito.when(documentProcessingService.getTotalVectorCount()).thenReturn(TOTAL_VECTOR_COUNT);

        AdminStatsDto stats = adminService.getSystemStats();

        Assertions.assertEquals(RUNNING_JOBS, stats.getCurrentlyProcessing());
        Assertions.assertEquals(EXPECTED_SUCCESS_RATE, stats.getSuccessRate());
        Assertions.assertEquals(EXPECTED_FORMATTED_VECTORS, stats.getTotalVectors());
    }
    @Test
    public void testGetFailedJobsShouldMapCorrectly() {
        Document testDoc = new Document();
        testDoc.setFilename(TEST_FILENAME);
        testDoc.setUploadedAt(LocalDateTime.now());

        ProcessingJob job = new ProcessingJob();
        job.setId(TEST_JOB_ID);
        job.setDocument(testDoc);
        job.setStage(ProcessingJobStage.FAILED);

        ProcessingJobDto mockDto = new ProcessingJobDto();

        Mockito.when(processingJobService.findAllFailedJobs()).thenReturn(List.of(job));
        Mockito.when(modelMapper.map(any(ProcessingJob.class), eq(ProcessingJobDto.class))).thenReturn(mockDto);
        Mockito.when(chatService.findUserEmailByDocument(testDoc)).thenReturn(TEST_USER_EMAIL);

        List<ProcessingJobDto> failedJobs = adminService.getFailedJobs();

        Assertions.assertFalse(failedJobs.isEmpty());
        ProcessingJobDto resultDto = failedJobs.get(0);
        Assertions.assertEquals(EXPECTED_JOB_ID_DISPLAY, resultDto.getJobId());
        Assertions.assertEquals(TEST_USER_EMAIL, resultDto.getUserEmail());
        Assertions.assertEquals(TEST_FILENAME, resultDto.getFileName());
    }
}