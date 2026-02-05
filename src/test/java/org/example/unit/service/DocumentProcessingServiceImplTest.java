package org.example.unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.example.services.ProcessingJobService;
import org.example.repositories.DocumentChunkRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import java.util.concurrent.Executor;
import org.example.services.impl.DocumentProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.Document;
import org.mockito.MockedStatic;
import org.example.utils.FileUtils;
import org.example.utils.TextUtils;
import java.util.List;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.models.entities.DocumentChunk;
import static org.mockito.ArgumentMatchers.*;
@ExtendWith(MockitoExtension.class)
public class DocumentProcessingServiceImplTest {
    private static final Long TEST_DOC_ID = 1L;
    private static final int TOP_K = 5;
    private static final long TOTAL_COUNT = 100L;
    private static final String MOCK_TEXT = "Sample PDF content";
    @Mock
    private ProcessingJobService processingJobService;
    @Mock
    private DocumentChunkRepository documentChunkRepository;
    @Mock
    private EmbeddingModel embeddingModel;
    @Mock
    private Executor taskExecutor;
    private DocumentProcessingServiceImpl documentProcessingService;
    @BeforeEach
    public void setUp() {
        documentProcessingService = new DocumentProcessingServiceImpl(
                processingJobService,
                documentChunkRepository,
                embeddingModel,
                taskExecutor
        );
    }
    @Test
    public void testProcessDocumentShouldFlowThroughAllStages() {
        ProcessingJob job = new ProcessingJob();
        Document doc = new Document();
        doc.setId(TEST_DOC_ID);
        job.setDocument(doc);

        try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class);
             MockedStatic<TextUtils> textUtils = Mockito.mockStatic(TextUtils.class)) {

            Mockito.when(processingJobService.findByDocumentId(TEST_DOC_ID)).thenReturn(job);
            fileUtils.when(() -> FileUtils.extractTextFromPdf(any())).thenReturn(MOCK_TEXT);
            textUtils.when(() -> TextUtils.prepareSemanticChunks(eq(MOCK_TEXT), anyInt())).thenReturn(List.of("chunk1"));

            Mockito.doAnswer(invocation -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }).when(taskExecutor).execute(any(Runnable.class));

            Mockito.when(embeddingModel.embed(anyString())).thenReturn(List.of(0.1, 0.2));

            documentProcessingService.processDocument(TEST_DOC_ID);

            Assertions.assertEquals(ProcessingJobStage.COMPLETED, job.getStage());
            Mockito.verify(documentChunkRepository).saveAll(any());
            Mockito.verify(processingJobService, Mockito.atLeastOnce()).saveProcessingJob(job);
        }
    }
    @Test
    public void testProcessDocumentShouldHandleFailure() {
        ProcessingJob job = new ProcessingJob();
        job.setDocument(new Document());

        Mockito.when(processingJobService.findByDocumentId(anyLong())).thenReturn(job);

        try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
            fileUtils.when(() -> FileUtils.extractTextFromPdf(any())).thenThrow(new RuntimeException("PDF Error"));

            documentProcessingService.processDocument(TEST_DOC_ID);

            Assertions.assertEquals(ProcessingJobStage.FAILED, job.getStage());
            Assertions.assertEquals("PDF Error", job.getErrorMessage());
        }
    }
    @Test
    public void testGetTotalVectorCountShouldReturnCorrectValue() {
        Mockito.when(documentChunkRepository.count()).thenReturn(TOTAL_COUNT);
        long result = documentProcessingService.getTotalVectorCount();
        Assertions.assertEquals(TOTAL_COUNT, result);
    }
    @Test
    public void testFindTopSimilarShouldInvokeRepository() {
        float[] queryVector = new float[]{0.1f, 0.2f};
        Mockito.when(documentChunkRepository.findTopSimilar(eq(TEST_DOC_ID), eq(queryVector), eq(TOP_K)))
                .thenReturn(List.of());

        documentProcessingService.findTopSimilar(TEST_DOC_ID, queryVector, TOP_K);

        Mockito.verify(documentChunkRepository).findTopSimilar(TEST_DOC_ID, queryVector, TOP_K);
    }
    @Test
    public void testGetChunkByIdShouldReturnReference() {
        DocumentChunk mockChunk = new DocumentChunk();
        Mockito.when(documentChunkRepository.getReferenceById(1L)).thenReturn(mockChunk);

        DocumentChunk result = documentProcessingService.getChunkById(1L);

        Assertions.assertEquals(mockChunk, result);
    }
}