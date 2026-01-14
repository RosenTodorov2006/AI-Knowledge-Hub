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
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private final ProcessingJobRepository processingJobRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingModel embeddingModel;

    public DocumentProcessingServiceImpl(ProcessingJobRepository processingJobRepository, DocumentChunkRepository documentChunkRepository, EmbeddingModel embeddingModel) {
        this.processingJobRepository = processingJobRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingModel = embeddingModel;
    }
    @Override
    @Async
    @Transactional
    public void processDocument(Long documentId) {
        ProcessingJob job = processingJobRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        Document document = job.getDocument();

        try {
            //parsing
            updateJobStage(job, ProcessingJobStage.PARSING);
            String text = extractText(document.getContent());
            //indexing
            updateJobStage(job, ProcessingJobStage.INDEXING);
            processChunks(document, text);
            //completed
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

    private void processChunks(Document document, String text) {
        int chunkSize = 800; // Символи
        int overlap = 150;
        int start = 0;
        int index = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String content = text.substring(start, end);

            // Генерираме вектора ЛОКАЛНО
            List<Double> vector = embeddingModel.embed(content);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocument(document);
            chunk.setChunkIndex(index++);
            chunk.setContent(content);
            chunk.setEmbedding(vector); // Твоята vector колона в БД
            chunk.setTokenCount(content.split("\\s+").length);

            documentChunkRepository.save(chunk);

            if (end == text.length()) break;
            start += (chunkSize - overlap);
        }
    }

    private void updateJobStage(ProcessingJob job, ProcessingJobStage stage) {
        job.setStage(stage);
        processingJobRepository.save(job);
    }
}
