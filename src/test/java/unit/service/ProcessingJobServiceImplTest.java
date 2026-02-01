package unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.example.repositories.ProcessingJobRepository;
import org.springframework.context.MessageSource;
import org.example.services.impl.ProcessingJobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.models.entities.ProcessingJob;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import static org.mockito.ArgumentMatchers.any;
import org.example.models.entities.Document;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import org.example.models.entities.enums.ProcessingJobStage;
@ExtendWith(MockitoExtension.class)
public class ProcessingJobServiceImplTest {
    private static final long TEST_DOC_ID = 1L;
    private static final long TEST_COUNT = 5L;
    private static final String ERR_NOT_FOUND = "Job not found";
    @Mock
    private ProcessingJobRepository processingJobRepository;
    @Mock
    private MessageSource messageSource;
    private ProcessingJobServiceImpl processingJobService;
    @BeforeEach
    public void setUp() {
        processingJobService = new ProcessingJobServiceImpl(processingJobRepository, messageSource);
    }
    @Test
    public void testFindByDocumentIdShouldReturnJobWhenExists() {
        ProcessingJob mockJob = new ProcessingJob();
        when(processingJobRepository.findByDocumentId(TEST_DOC_ID)).thenReturn(Optional.of(mockJob));

        ProcessingJob result = processingJobService.findByDocumentId(TEST_DOC_ID);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockJob, result);
    }
    @Test
    public void testFindByDocumentIdShouldThrowExceptionWhenNotFound() {
        when(processingJobRepository.findByDocumentId(TEST_DOC_ID)).thenReturn(Optional.empty());
        when(messageSource.getMessage(any(), any(), any())).thenReturn(ERR_NOT_FOUND);

        Assertions.assertThrows(RuntimeException.class, () ->
                processingJobService.findByDocumentId(TEST_DOC_ID));
    }
    @Test
    public void testCreateProcessingJobShouldSetInitialStage() {
        Document mockDoc = Mockito.mock(Document.class);
        ArgumentCaptor<ProcessingJob> jobCaptor = ArgumentCaptor.forClass(ProcessingJob.class);

        processingJobService.createProcessingJob(mockDoc);

        verify(processingJobRepository).save(jobCaptor.capture());
        ProcessingJob capturedJob = jobCaptor.getValue();

        Assertions.assertEquals(mockDoc, capturedJob.getDocument());
        Assertions.assertEquals(ProcessingJobStage.UPLOADED, capturedJob.getStage());
    }
    @Test
    public void testCountMethodsShouldReturnRepositoryValues() {
        when(processingJobRepository.countByStage(ProcessingJobStage.COMPLETED)).thenReturn(TEST_COUNT);
        when(processingJobRepository.countByStageNotAndErrorMessageIsNull(ProcessingJobStage.COMPLETED)).thenReturn(TEST_COUNT);
        when(processingJobRepository.countByErrorMessageIsNotNull()).thenReturn(TEST_COUNT);

        Assertions.assertEquals(TEST_COUNT, processingJobService.countCompletedJobs());
        Assertions.assertEquals(TEST_COUNT, processingJobService.countRunningJobs());
        Assertions.assertEquals(TEST_COUNT, processingJobService.countFailedJobs());
    }
}