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
    private final ProcessingJobRepository processingJobRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingModel embeddingModel;
    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    public DocumentProcessingServiceImpl(ProcessingJobRepository processingJobRepository,
                                         DocumentChunkRepository documentChunkRepository,
                                         EmbeddingModel embeddingModel, Executor taskExecutor) {
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
                .orElseThrow(() -> new RuntimeException("Job not found"));
        Document document = job.getDocument();

        try {
            updateJobStage(job, ProcessingJobStage.PARSING);
            String text = extractText(document.getContent());

            updateJobStage(job, ProcessingJobStage.INDEXING);
            processChunks(document, text);

            updateJobStage(job, ProcessingJobStage.COMPLETED);
        } catch (Exception e) {
            job.setStage(ProcessingJobStage.FAILED);
            job.setErrorMessage(e.getMessage());
            processingJobRepository.save(job);
        }
    }

    private String extractText(byte[] content) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(pdf);
        }
    }

    private List<String> prepareSemanticChunks(String text, int limit) {
        List<String> chunks = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(text);

        StringBuilder currentChunk = new StringBuilder();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end);

            if (currentChunk.length() + sentence.length() > limit && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());

                int lastDot = currentChunk.lastIndexOf(". ");
                String context = (lastDot != -1) ? currentChunk.substring(lastDot + 1) : "";
                currentChunk = new StringBuilder(context);
            }
            currentChunk.append(sentence);
        }
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }

    private void processChunks(Document document, String text) {
        List<String> semanticChunks = prepareSemanticChunks(text, 800);
        Long docId = document.getId();

        List<CompletableFuture<DocumentChunk>> futures = semanticChunks.stream()
                .map(content -> CompletableFuture.supplyAsync(() -> {
                    List<Double> vector = embeddingModel.embed(content);

                    DocumentChunk chunk = new DocumentChunk();

                    Document docProxy = new Document();
                    docProxy.setId(docId);
                    chunk.setDocument(docProxy);

                    chunk.setContent(content);
                    chunk.setEmbedding(vector);
                    chunk.setTokenCount(content.split("\\s+").length);

                    return chunk;
                }, taskExecutor))
                .toList();

        List<DocumentChunk> allChunks = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        documentChunkRepository.saveAll(allChunks);
        documentChunkRepository.flush();
    }

    private void updateJobStage(ProcessingJob job, ProcessingJobStage stage) {
        job.setStage(stage);
        processingJobRepository.save(job);
    }
}