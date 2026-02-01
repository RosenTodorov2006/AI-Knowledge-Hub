package unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.example.repositories.DocumentRepository;
import org.example.services.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.Mockito;
import org.example.models.entities.Document;
import org.example.models.entities.enums.DocumentStatus;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Assertions;
@ExtendWith(MockitoExtension.class)
public class DocumentServiceImplTest {
    private static final String TEST_FILENAME = "knowledge.pdf";
    private static final String TEST_CONTENT_TYPE = "application/pdf";
    private static final byte[] TEST_BYTES = "sample content".getBytes();
    @Mock
    private DocumentRepository documentRepository;
    private DocumentServiceImpl documentService;
    @BeforeEach
    public void setUp() {
        documentService = new DocumentServiceImpl(documentRepository);
    }
    @Test
    public void testSaveDocumentShouldReturnCorrectDocument() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Document savedDocument = new Document();
        savedDocument.setFilename(TEST_FILENAME);
        savedDocument.setDocumentStatus(DocumentStatus.UPLOADED);

        Mockito.when(mockFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
        Mockito.when(mockFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);
        Mockito.when(mockFile.getBytes()).thenReturn(TEST_BYTES);
        Mockito.when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        Document result = documentService.saveDocument(mockFile);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(TEST_FILENAME, result.getFilename());
        Assertions.assertEquals(DocumentStatus.UPLOADED, result.getDocumentStatus());

        Mockito.verify(mockFile).getOriginalFilename();
        Mockito.verify(mockFile).getContentType();
        Mockito.verify(mockFile).getBytes();
        Mockito.verify(documentRepository).save(any(Document.class));
    }
}