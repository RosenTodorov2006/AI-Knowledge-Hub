package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.models.entities.Document;
import org.example.models.entities.DocumentChunk;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.enums.DocumentStatus;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.ChunkSearchResult;
import org.example.repositories.DocumentChunkRepository;
import org.example.repositories.ProcessingJobRepository;
import org.example.services.DocumentProcessingService;
import org.example.services.DocumentService;
import org.example.services.ProcessingJobService;
import org.example.utils.FileUtils;
import org.example.utils.TextUtils;
import org.example.validation.annotation.TrackProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.context.i18n.LocaleContextHolder;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private static final int CHUNK_CHARACTER_LIMIT = 800;
    private final ProcessingJobService processingJobService;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingModel embeddingModel;
    private final SimpMessagingTemplate messagingTemplate;
    private final DocumentService documentService;
    private final MessageSource messageSource;
    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    public DocumentProcessingServiceImpl(ProcessingJobService processingJobService,
                                         DocumentChunkRepository documentChunkRepository,
                                         EmbeddingModel embeddingModel, SimpMessagingTemplate messagingTemplate, DocumentService documentService, MessageSource messageSource,
                                         Executor taskExecutor) {
        this.processingJobService = processingJobService;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingModel = embeddingModel;
        this.messagingTemplate = messagingTemplate;
        this.documentService = documentService;
        this.messageSource = messageSource;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Async
    @Transactional
    @TrackProcessing
    public void processDocument(Long documentId) {
        log.info(">>> КРИТИЧЕН ТЕСТ: Нишката влезе в processDocument за ID: {}", documentId);

        ProcessingJob job = processingJobService.findByDocumentId(documentId);

        if (job == null) {
            log.error("КРИТИЧНА ГРЕШКА: Задачата не е намерена в базата за ID: {}", documentId);
            return;
        }

        Document document = job.getDocument();
        if (document == null) {
            log.error("КРИТИЧНА ГРЕШКА: Документът е null за задача ID: {}", job.getId());
            return;
        }

        log.info("Документът е зареден: {} (MIME: {}). Започване на PARSING...",
                document.getFilename(), document.getMimeType());

        try {
            log.info("Стъпка 1: Обновяване на статус на PARSING...");
            updateJobStage(job, ProcessingJobStage.PARSING);

            String text = extractTextBasedOnType(document);

            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("Извлеченият текст е празен или NULL! Провери метода extractTextBasedOnType.");
            }

            log.info("Текстът е извлечен успешно. Дължина на символите: {}", text.length());

            log.info("Стъпка 2: Обновяване на статус на INDEXING...");
            updateJobStage(job, ProcessingJobStage.INDEXING);

            processChunks(document, text);
            log.info("Векторизацията и записът в PGVector приключиха.");

            log.info("Стъпка 3: Обновяване на статус на COMPLETED.");
            updateJobStage(job, ProcessingJobStage.COMPLETED);
            log.info(">>> УСПЕШЕН КРАЙ на обработката за документ ID: {}", documentId);

        } catch (Exception e) {
            log.error("!!! ГРЕШКА ПРИ ОБРАБОТКА на документ " + documentId + ": " + e.getMessage(), e);
            handleProcessingFailure(job, e);
        }
    }

    @Override
    public DocumentChunk getChunkById(long id) {
        return documentChunkRepository.getReferenceById(id);
    }

    @Override
    public List<ChunkSearchResult> findTopSimilar(long documentId, float[] queryVector, int defaultTopK) {
        return documentChunkRepository.findTopSimilar(documentId, queryVector, defaultTopK);
    }

    @Override
    public long getTotalVectorCount() {
        return documentChunkRepository.count();
    }

    private void processChunks(Document document, String text) {
        log.info(">>> [INDEXING] Начало на раздробяване за файл: {}", document.getFilename());

        List<String> semanticChunks = TextUtils.prepareSemanticChunks(text, CHUNK_CHARACTER_LIMIT);
        log.info(">>> [INDEXING] Текстът е разделен на {} парчета. Започвам ПОСЛЕДОВАТЕЛНА векторизация...", semanticChunks.size());

        List<DocumentChunk> allChunks = new ArrayList<>();
        int count = 0;

        for (String content : semanticChunks) {
            count++;
            log.info(">>> [INDEXING] Обработка на парче {}/{} (дължина: {} символа)",
                    count, semanticChunks.size(), content.length());

            try {
                DocumentChunk chunk = createChunkWithEmbedding(document.getId(), content);
                allChunks.add(chunk);
                log.info(">>> [INDEXING] Парче {} е успешно векторизирано.", count);
            } catch (Exception e) {
                log.error("!!! [INDEXING] КРИТИЧНА ГРЕШКА при парче {}: {}", count, e.getMessage());
                throw new RuntimeException("Грешка при векторизация на парче " + count, e);
            }
        }

        log.info(">>> [INDEXING] Всички {} парчета са готови. Запис в базата данни (document_chunks)...", allChunks.size());
        documentChunkRepository.saveAll(allChunks);
        log.info(">>> [INDEXING] УСПЕШЕН ЗАПИС в базата данни.");
    }

    private DocumentChunk createChunkWithEmbedding(Long docId, String content) {
        List<Double> vector = embeddingModel.embed(content);

        DocumentChunk chunk = new DocumentChunk();
        Document docProxy = new Document();
        docProxy.setId(docId);

        chunk.setDocument(docProxy);
        chunk.setContent(content);
        chunk.setEmbedding(vector);
        chunk.setTokenCount(TextUtils.calculateTokenCount(content));

        return chunk;
    }

    private void updateJobStage(ProcessingJob job, ProcessingJobStage stage) {
        job.setStage(stage);
        processingJobService.saveProcessingJob(job);
    }

    private void handleProcessingFailure(ProcessingJob job, Exception e) {
        job.setStage(ProcessingJobStage.FAILED);
        job.setErrorMessage(e.getMessage());
        processingJobService.saveProcessingJob(job);
        Document document = job.getDocument();
        this.documentService.updateDocumentStatus(document, DocumentStatus.FAILED);

        messagingTemplate.convertAndSend("/topic/doc-status/" + job.getDocument().getId(),
                Map.of("status", "FAILED", "message", e.getMessage()));
    }

    private String extractTextBasedOnType(Document document) throws IOException {
        String fileName = document.getFilename().toLowerCase();
        byte[] content = document.getContent();

        if (fileName.endsWith(".pdf")) {
            return FileUtils.extractTextFromPdf(content);
        } else if (fileName.endsWith(".docx")) {
            return FileUtils.extractTextFromDocx(content);
        } else if (fileName.endsWith(".doc")) {
            return FileUtils.extractTextFromDocLegacy(content);
        } else if (fileName.endsWith(".pptx")) {
            return FileUtils.extractTextFromPptx(content);
        } else if (fileName.endsWith(".ppt")) {
            return FileUtils.extractTextFromPptLegacy(content);
        } else if (fileName.endsWith(".txt")) {
            return FileUtils.extractTextFromTxt(content);
        } else {
            String originalFileName = document.getFilename();
            String translatedMessage = messageSource.getMessage(
                    "error.unsupported.format",
                    new Object[]{originalFileName},
                    LocaleContextHolder.getLocale()
            );
            throw new IllegalArgumentException(translatedMessage);
        }
    }
}