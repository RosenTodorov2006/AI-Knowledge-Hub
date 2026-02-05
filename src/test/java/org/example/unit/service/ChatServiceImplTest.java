package org.example.unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.example.repositories.ChatRepository;
import org.example.services.DocumentProcessingService;
import org.example.services.MessageService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.modelmapper.ModelMapper;
import org.example.services.UserService;
import org.example.services.ProcessingJobService;
import org.example.services.DocumentService;
import org.example.clients.OpenAiClient;
import org.springframework.context.MessageSource;
import org.example.services.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.Mockito;
import org.example.models.entities.UserEntity;
import org.example.models.entities.Document;
import org.example.models.entities.Chat;
import org.example.models.dtos.exportDtos.ChatViewDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.Assertions;
import org.example.models.entities.Message;
import org.example.repositories.ChunkSearchResult;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import org.example.models.entities.enums.MessageRole;
import java.util.List;
import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import org.example.models.entities.MessageContextSource;
import org.example.models.entities.DocumentChunk;

@ExtendWith(MockitoExtension.class)
public class ChatServiceImplTest {
    private static final String USER_EMAIL = "test@example.com";
    private static final Long CHAT_ID = 1L;
    private static final Long DOC_ID = 100L;
    private static final String DOC_FILENAME = "manual.pdf";
    private static final String USER_QUERY = "Какво е векторизация?";
    private static final String AI_TEXT_RESPONSE = "Векторизацията е процес...";
    private static final String THREAD_ID = "thread_abc123";
    private static final Long AI_MESSAGE_ID = 500L;
    private static final String ERR_NOT_FOUND = "Chat not found";
    @Mock private ChatRepository chatRepository;
    @Mock private DocumentProcessingService documentProcessingService;
    @Mock private MessageService messageService;
    @Mock private EmbeddingModel embeddingModel;
    @Mock private ModelMapper modelMapper;
    @Mock private UserService userService;
    @Mock private ProcessingJobService processingJobService;
    @Mock private DocumentService documentService;
    @Mock private OpenAiClient openAiClient;
    @Mock private MessageSource messageSource;
    private ChatServiceImpl chatService;
    @BeforeEach
    public void setUp() {
        chatService = new ChatServiceImpl(
                chatRepository, documentProcessingService, messageService,
                embeddingModel, modelMapper, userService,
                processingJobService, documentService, openAiClient, messageSource
        );
    }
    @Test
    public void testStartNewChatShouldInitializeCorrectly() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        UserEntity mockUser = new UserEntity();
        Document mockDoc = new Document();
        mockDoc.setId(DOC_ID);
        mockDoc.setFilename(DOC_FILENAME);
        Chat mockChat = new Chat();
        ChatViewDto mockDto = new ChatViewDto();

        Mockito.when(userService.findUserByEmail(USER_EMAIL)).thenReturn(mockUser);
        Mockito.when(documentService.saveDocument(mockFile)).thenReturn(mockDoc);
        Mockito.when(chatRepository.save(any(Chat.class))).thenReturn(mockChat);
        Mockito.when(modelMapper.map(any(Chat.class), eq(ChatViewDto.class))).thenReturn(mockDto);

        ChatViewDto result = chatService.startNewChat(mockFile, USER_EMAIL);

        Assertions.assertNotNull(result);
        Mockito.verify(processingJobService).createProcessingJob(mockDoc);
        Mockito.verify(documentProcessingService).processDocument(DOC_ID);
    }
    @Test
    public void testGenerateResponseShouldExecuteFullRAGFlow() {
        Chat mockChat = createMockChat();
        Message mockUserMessage = new Message();
        Message mockAiMessage = new Message();
        mockAiMessage.setId(AI_MESSAGE_ID);
        mockAiMessage.setContent(AI_TEXT_RESPONSE);
        ChunkSearchResult mockSearchResult = Mockito.mock(ChunkSearchResult.class);

        Mockito.when(chatRepository.findById(CHAT_ID)).thenReturn(Optional.of(mockChat));
        Mockito.when(messageService.saveMessage(eq(mockChat), eq(USER_QUERY), eq(MessageRole.USER))).thenReturn(mockUserMessage);
        Mockito.when(embeddingModel.embed(anyString())).thenReturn(List.of(0.1, 0.2));
        Mockito.when(documentProcessingService.findTopSimilar(eq(DOC_ID), any(), anyInt())).thenReturn(List.of(mockSearchResult));
        Mockito.when(openAiClient.askAssistant(eq(THREAD_ID), anyString())).thenReturn(AI_TEXT_RESPONSE);
        Mockito.when(messageService.saveMessage(eq(mockChat), eq(AI_TEXT_RESPONSE), eq(MessageRole.ASSISTANT))).thenReturn(mockAiMessage);

        ChatResponseDto result = chatService.generateResponse(CHAT_ID, USER_QUERY);

        Assertions.assertEquals(AI_TEXT_RESPONSE, result.getAnswer());
        Mockito.verify(messageService).saveMessageSources(eq(mockAiMessage), any());
    }
    @Test
    public void testGetChatDetailsShouldMapFullResponse() {
        Chat mockChat = createMockChat();
        Message mockMsg = new Message();
        mockMsg.setRole(MessageRole.ASSISTANT);
        mockMsg.setContent("AI Answer");
        mockMsg.setCreatedAt(LocalDateTime.now());

        MessageContextSource source = new MessageContextSource();
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Source Text");
        source.setChunk(chunk);
        mockMsg.setContextSources(List.of(source));
        mockChat.setMessages(List.of(mockMsg));

        ChatViewDto mockDto = new ChatViewDto();

        Mockito.when(chatRepository.findByIdAndUserEntityEmail(CHAT_ID, USER_EMAIL)).thenReturn(Optional.of(mockChat));
        Mockito.when(modelMapper.map(any(Chat.class), eq(ChatViewDto.class))).thenReturn(mockDto);

        ChatViewDto result = chatService.getChatDetails(CHAT_ID, USER_EMAIL);

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.getMessages().isEmpty());
        Assertions.assertEquals("Source Text", result.getMessages().get(0).getSources().get(0));
    }
    @Test
    public void testGetChatDetailsShouldThrowForbiddenWhenNotOwner() {
        Mockito.when(chatRepository.findByIdAndUserEntityEmail(CHAT_ID, USER_EMAIL)).thenReturn(Optional.empty());
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Denied");

        Assertions.assertThrows(ResponseStatusException.class, () -> chatService.getChatDetails(CHAT_ID, USER_EMAIL));
    }
    private Chat createMockChat() {
        UserEntity user = new UserEntity();
        user.setEmail(USER_EMAIL);
        Document doc = new Document();
        doc.setId(DOC_ID);
        doc.setFilename(DOC_FILENAME);
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        chat.setUser(user);
        chat.setDocument(doc);
        chat.setOpenAiThreadId(THREAD_ID);
        return chat;
    }
}