package org.example.services.impl;

import org.example.models.entities.Document;
import org.example.models.entities.enums.DocumentStatus;
import org.example.repositories.DocumentRepository;
import org.example.services.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document saveDocument(MultipartFile file) throws IOException {
        Document document = new Document(file.getOriginalFilename(),
                file.getContentType(), DocumentStatus.UPLOADED,
                LocalDateTime.now(), file.getBytes());
        return documentRepository.save(document);
    }

    @Override
    public void updateDocumentStatus(Document document, DocumentStatus documentStatus) {
        document.setDocumentStatus(DocumentStatus.FAILED);
        documentRepository.save(document);
    }


}
