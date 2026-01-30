package org.example.services;

import org.example.models.entities.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentService {
    Document saveDocument(MultipartFile file) throws IOException;
}
