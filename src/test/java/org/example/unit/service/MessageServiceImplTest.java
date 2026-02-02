package org.example.unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.models.entities.enums.MessageRole;
import org.mockito.Mock;
import org.example.repositories.MessageRepository;
import org.example.services.MessageContextSourceService;
import org.example.services.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.models.entities.Chat;
import org.mockito.Mockito;
import org.example.models.entities.Message;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Assertions;
import org.example.repositories.ChunkSearchResult;
import java.util.List;
import static org.mockito.ArgumentMatchers.eq;
@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {
    private static final String TEST_CONTENT = "Как работи AI?";
    private static final MessageRole TEST_ROLE = MessageRole.USER;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageContextSourceService messageContextSourceService;
    private MessageServiceImpl messageService;
    @BeforeEach
    public void setUp() {
        messageService = new MessageServiceImpl(messageRepository, messageContextSourceService);
    }
    @Test
    public void testSaveMessageShouldPopulateFieldsAndSave() {
        Chat mockChat = Mockito.mock(Chat.class);
        Message mockSavedMessage = new Message();
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        when(messageRepository.save(any(Message.class))).thenReturn(mockSavedMessage);

        Message result = messageService.saveMessage(mockChat, TEST_CONTENT, TEST_ROLE);

        verify(messageRepository).save(messageCaptor.capture());
        Message capturedMessage = messageCaptor.getValue();

        Assertions.assertEquals(TEST_CONTENT, capturedMessage.getContent());
        Assertions.assertEquals(TEST_ROLE, capturedMessage.getRole());
        Assertions.assertEquals(mockChat, capturedMessage.getChat());
        Assertions.assertNotNull(capturedMessage.getCreatedAt());
        Assertions.assertEquals(mockSavedMessage, result);
    }
    @Test
    public void testSaveMessageSourcesShouldInvokeSourceServiceForEachResult() {
        Message mockMessage = Mockito.mock(Message.class);
        ChunkSearchResult result1 = Mockito.mock(ChunkSearchResult.class);
        ChunkSearchResult result2 = Mockito.mock(ChunkSearchResult.class);
        List<ChunkSearchResult> searchResults = List.of(result1, result2);

        messageService.saveMessageSources(mockMessage, searchResults);

        verify(messageContextSourceService, times(1)).saveSource(mockMessage, result1);
        verify(messageContextSourceService, times(1)).saveSource(mockMessage, result2);
        verify(messageContextSourceService, times(2)).saveSource(eq(mockMessage), any());
    }
}