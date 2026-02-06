package org.example.services;

import org.example.models.entities.Document;
import org.example.models.entities.enums.DocumentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {
    Document saveDocument(MultipartFile file) throws IOException;
    void updateDocumentStatus(Document document, DocumentStatus documentStatus);
}
