package org.example.integration.rest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.integration.base.BaseIntegrationTest;
import org.example.models.dtos.importDtos.ChatRequestDto;
import org.example.models.entities.Chat;
import org.example.models.entities.Document;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.ApplicationRole;
import org.example.models.entities.enums.DocumentStatus;
import org.example.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc
public class ChatRestControllerIntegrationTest extends BaseIntegrationTest {
    private static final String API_BASE = "/api/chats";
    private static final String CREATE_PATH = "/api/chats/create";
    private static final String USER_EMAIL = "user@test.bg";
    private static final String TEST_PASSWORD = "password";
    private static final String MULTIPART_FILE_NAME = "file";
    private static final String FILE_NAME = "test.pdf";
    private static final String FILE_CONTENT_TYPE = "application/pdf";
    private static final String EMPTY_FILE_CONTENT_TYPE = "text/plain";
    private static final String EMPTY_FILENAME = "";
    private static final String CHAT_MESSAGE = "Здравей, AI!";
    private static final String AI_RESPONSE = "Здравей! Аз съм твоят асистент.";
    private static final String CHAT_TITLE = "Test Chat";
    private static final String THREAD_ID = "thread_123";
    private static final String JSON_PATH_ID = "$.id";
    private static final String JSON_PATH_ERROR = "$.error";
    private static final String JSON_PATH_ANSWER = "$.answer";
    private static final byte[] FILE_BYTES = "content".getBytes();
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final int EMBEDDING_LIMIT = 384;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private org.example.clients.OpenAiClient openAiClient;
    @SpyBean
    private DocumentChunkRepository documentChunkRepository;
    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        chatRepository.deleteAll();
        documentRepository.deleteAll();
        userRepository.deleteAll();
        UserEntity testUser = new UserEntity();
        testUser.setUsername(USER_EMAIL);
        testUser.setEmail(USER_EMAIL);
        testUser.setPassword(TEST_PASSWORD);
        testUser.setActive(true);
        testUser.setRole(ApplicationRole.USER);
        userRepository.save(testUser);
        when(embeddingModel.embed(any(String.class)))
                .thenAnswer(invocation -> java.util.stream.DoubleStream.generate(Math::random)
                        .limit(EMBEDDING_LIMIT)
                        .boxed()
                        .collect(java.util.stream.Collectors.toList()));
        doReturn(java.util.List.of())
                .when(documentChunkRepository)
                .findTopSimilar(anyLong(), any(float[].class), anyInt());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    void testGetDashboardData_ShouldReturnList() throws Exception {
        mockMvc.perform(get(API_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    void testCreateChat_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                MULTIPART_FILE_NAME, FILE_NAME, FILE_CONTENT_TYPE, FILE_BYTES);
        mockMvc.perform(multipart(CREATE_PATH)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(JSON_PATH_ID).exists());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    void testCreateChat_EmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(MULTIPART_FILE_NAME, EMPTY_FILENAME, EMPTY_FILE_CONTENT_TYPE, EMPTY_BYTES);
        mockMvc.perform(multipart(CREATE_PATH)
                        .file(emptyFile)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_PATH_ERROR).exists());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    void testSendMessage_Success() throws Exception {
        Document doc = createAndSaveDocument();
        Chat chat = createAndSaveChat(doc);
        ChatRequestDto request = new ChatRequestDto();
        request.setMessage(CHAT_MESSAGE);
        when(openAiClient.askAssistant(anyString(), anyString()))
                .thenReturn(AI_RESPONSE);
        mockMvc.perform(post(API_BASE + "/" + chat.getId() + "/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_ANSWER).value(AI_RESPONSE));
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    void testGetChatDetails_Success() throws Exception {
        Document doc = createAndSaveDocument();
        Chat chat = createAndSaveChat(doc);
        mockMvc.perform(get(API_BASE + "/" + chat.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_ID).value(chat.getId()));
    }
    private Document createAndSaveDocument() {
        Document doc = new Document();
        doc.setContent(FILE_BYTES);
        doc.setFilename(FILE_NAME);
        doc.setMimeType(FILE_CONTENT_TYPE);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setDocumentStatus(DocumentStatus.INDEXED);
        return documentRepository.save(doc);
    }
    private Chat createAndSaveChat(Document doc) {
        Chat chat = new Chat();
        chat.setUser(userRepository.findByUsername(USER_EMAIL).get());
        chat.setDocument(doc);
        chat.setTitle(CHAT_TITLE);
        chat.setLastMessageAt(LocalDateTime.now());
        chat.setOpenAiThreadId(THREAD_ID);
        return chatRepository.save(chat);
    }
}