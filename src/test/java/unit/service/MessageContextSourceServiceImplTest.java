package unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.example.repositories.MessageContextSourceRepository;
import org.example.services.DocumentProcessingService;
import org.example.services.impl.MessageContextSourceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.models.entities.Message;
import org.mockito.Mockito;
import org.example.repositories.ChunkSearchResult;
import org.example.models.entities.DocumentChunk;
import org.mockito.ArgumentCaptor;
import org.example.models.entities.MessageContextSource;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Assertions;
@ExtendWith(MockitoExtension.class)
public class MessageContextSourceServiceImplTest {
    private static final long TEST_CHUNK_ID = 10L;
    private static final double TEST_SCORE = 0.95;
    @Mock
    private MessageContextSourceRepository messageContextSourceRepository;
    @Mock
    private DocumentProcessingService documentProcessingService;
    private MessageContextSourceServiceImpl messageContextSourceService;
    @BeforeEach
    public void setUp() {
        messageContextSourceService = new MessageContextSourceServiceImpl(
                messageContextSourceRepository,
                documentProcessingService
        );
    }
    @Test
    public void testSaveSourceShouldCorrectlyLinkMessageAndChunk() {
        Message mockMessage = Mockito.mock(Message.class);
        ChunkSearchResult mockResult = Mockito.mock(ChunkSearchResult.class);
        DocumentChunk mockChunk = new DocumentChunk();
        ArgumentCaptor<MessageContextSource> sourceCaptor = ArgumentCaptor.forClass(MessageContextSource.class);

        when(mockResult.getId()).thenReturn(TEST_CHUNK_ID);
        when(mockResult.getSimilarity()).thenReturn(TEST_SCORE);
        when(documentProcessingService.getChunkById(TEST_CHUNK_ID)).thenReturn(mockChunk);

        messageContextSourceService.saveSource(mockMessage, mockResult);

        verify(documentProcessingService).getChunkById(TEST_CHUNK_ID);
        verify(messageContextSourceRepository).save(sourceCaptor.capture());

        MessageContextSource savedSource = sourceCaptor.getValue();
        Assertions.assertEquals(mockMessage, savedSource.getMessage());
        Assertions.assertEquals(mockChunk, savedSource.getChunk());
        Assertions.assertEquals(TEST_SCORE, savedSource.getScore());
    }
}