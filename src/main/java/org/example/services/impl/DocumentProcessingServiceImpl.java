package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.models.entities.Document;
import org.example.models.entities.DocumentChunk;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.DocumentChunkRepository;
import org.example.repositories.ProcessingJobRepository;
import org.example.services.DocumentProcessingService;
import org.example.validation.annotation.TrackProcessing;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {

    private static final String ERR_JOB_NOT_FOUND = "Job not found";
    private static final String REGEX_WHITESPACE = "\\s+";
    private static final String SENTENCE_DOT_SPACE = ". ";

    private static final int CHUNK_CHARACTER_LIMIT = 800;
    private static final int DOT_OFFSET = 1;

    private final ProcessingJobRepository processingJobRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingModel embeddingModel;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    public DocumentProcessingServiceImpl(ProcessingJobRepository processingJobRepository,
                                         DocumentChunkRepository documentChunkRepository,
                                         EmbeddingModel embeddingModel,
                                         Executor taskExecutor) {
        this.processingJobRepository = processingJobRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingModel = embeddingModel;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Async
    @Transactional
    @TrackProcessing
    public void processDocument(Long documentId) {
        ProcessingJob job = processingJobRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException(ERR_JOB_NOT_FOUND));
        Document document = job.getDocument();

        try {
            updateJobStage(job, ProcessingJobStage.PARSING);
            String text = extractText(document.getContent());

            updateJobStage(job, ProcessingJobStage.INDEXING);
            processChunks(document, text);

            updateJobStage(job, ProcessingJobStage.COMPLETED);
        } catch (Exception e) {
            handleProcessingFailure(job, e);
        }
    }

    private String extractText(byte[] content) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(pdf);
        }
    }

    private void processChunks(Document document, String text) {
        List<String> semanticChunks = prepareSemanticChunks(text, CHUNK_CHARACTER_LIMIT);

        List<CompletableFuture<DocumentChunk>> futures = semanticChunks.stream()
                .map(content -> CompletableFuture.supplyAsync(
                        () -> createChunkWithEmbedding(document.getId(), content),
                        taskExecutor))
                .toList();

        List<DocumentChunk> allChunks = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        documentChunkRepository.saveAll(allChunks);
        documentChunkRepository.flush();
    }

    private DocumentChunk createChunkWithEmbedding(Long docId, String content) {
        List<Double> vector = embeddingModel.embed(content);

        DocumentChunk chunk = new DocumentChunk();
        Document docProxy = new Document();
        docProxy.setId(docId);

        chunk.setDocument(docProxy);
        chunk.setContent(content);
        chunk.setEmbedding(vector);
        chunk.setTokenCount(calculateTokenCount(content));

        return chunk;
    }

    private List<String> prepareSemanticChunks(String text, int limit) {
        List<String> chunks = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(text);

        StringBuilder currentChunk = new StringBuilder();
        int start = iterator.first();

        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end);

            if (shouldStartNewChunk(currentChunk, sentence, limit)) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = startNewChunkWithContext(currentChunk);
            }
            currentChunk.append(sentence);
        }

        addRemainingContent(chunks, currentChunk);
        return chunks;
    }

    private boolean shouldStartNewChunk(StringBuilder current, String sentence, int limit) {
        return current.length() + sentence.length() > limit && !current.isEmpty();
    }

    private StringBuilder startNewChunkWithContext(StringBuilder currentChunk) {
        int lastDotIndex = currentChunk.lastIndexOf(SENTENCE_DOT_SPACE);
        String context = (lastDotIndex != -1) ? currentChunk.substring(lastDotIndex + DOT_OFFSET) : "";
        return new StringBuilder(context);
    }

    private void addRemainingContent(List<String> chunks, StringBuilder currentChunk) {
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }
    }

    private int calculateTokenCount(String content) {
        return content.split(REGEX_WHITESPACE).length;
    }

    private void updateJobStage(ProcessingJob job, ProcessingJobStage stage) {
        job.setStage(stage);
        processingJobRepository.save(job);
    }

    private void handleProcessingFailure(ProcessingJob job, Exception e) {
        job.setStage(ProcessingJobStage.FAILED);
        job.setErrorMessage(e.getMessage());
        processingJobRepository.save(job);
    }
}