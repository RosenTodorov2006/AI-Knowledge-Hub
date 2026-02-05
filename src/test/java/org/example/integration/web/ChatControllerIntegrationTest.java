package org.example.integration.web;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.example.integration.base.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.example.repositories.DocumentRepository;
import org.example.repositories.ProcessingJobRepository;
import org.example.repositories.MessageRepository;
import org.example.repositories.ChatRepository;
import org.example.repositories.UserRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.example.repositories.DocumentChunkRepository;
import org.example.clients.OpenAiClient;
import org.springframework.mock.web.MockHttpSession;
import org.example.services.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.example.models.entities.UserEntity;
import java.time.LocalDateTime;
import org.example.models.entities.enums.ApplicationRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.anyString;
import java.util.stream.DoubleStream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import org.example.models.entities.Document;
import static org.assertj.core.api.Assertions.assertThat;
import org.example.models.entities.ProcessingJob;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.models.entities.DocumentChunk;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.example.models.entities.Message;
import org.example.models.entities.enums.MessageRole;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import org.example.controllers.web.ChatController;
import org.example.models.entities.Chat;
import org.example.models.entities.enums.DocumentStatus;
import static org.example.controllers.web.ChatController.SESSION_CHAT_ID;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerIntegrationTest extends BaseIntegrationTest {
    private static final String TEST_USER_EMAIL = "user@test.bg";
    private static final String TEST_USER_PASS = "password";
    private static final String TEST_USER_NAME = "Test User";
    private static final String AI_STUB_RESPONSE = "AI е клон на компютърните науки, който позволява на машините да учат от опит.";
    private static final String PDF_RESOURCE_PATH = "A_Brief_Introduction_To_AI.pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String USER_PROMPT = "Какво е изкуствен интелект според документа?";
    private static final String DASHBOARD_VIEW_NAME = "dashboard";
    private static final String CHAT_VIEW_NAME = "chat";
    private static final String CHAT_MODEL_ATTR = "chat";
    private static final String KICKBOX_TEXT = "Примерен текст за кикбокс.";
    private static final String KICKBOX_FILE_NAME = "test-kickbox.pdf";
    private static final String KICKBOX_CHAT_TITLE = "Test";
    private static final String KICKBOX_THREAD_ID = "thread_test_123";
    private static final String KICKBOX_AI_ANSWER = "Кикбоксът е страхотен боен спорт.";
    private static final String KICKBOX_USER_QUERY = "Какво е кикбокс?";
    private static final String DEFAULT_CHAT_TITLE = "Test Chat";
    private static final String DEFAULT_THREAD_ID = "thread_123";
    private static final String EMPTY_FILE_NAME = "empty.pdf";
    private static final String EXCEPTION_MSG = "Service process failed";
    private static final String BLANK_MESSAGE = "   ";
    private static final String VALID_MESSAGE_VAL = "Valid message";
    private static final String TIMEOUT_MSG = "OpenAI Timeout";
    private static final String TIMEOUT_QUERY = "Какво ще кажеш?";
    private static final String FLASH_ATTR_ERROR = "error";
    private static final int EMBEDDING_DIM = 384;
    private static final int AWAIT_TIMEOUT_30 = 30;
    private static final int AWAIT_TIMEOUT_10 = 10;
    private static final int POLL_INTERVAL_MS = 500;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ProcessingJobRepository processingJobRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserRepository userRepository;
    @SpyBean
    private DocumentChunkRepository documentChunkRepository;
    @MockBean
    private OpenAiClient openAiClient;
    @SpyBean
    private ChatService chatService;
    private MockHttpSession session;
    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        documentChunkRepository.deleteAll();
        processingJobRepository.deleteAll();
        chatRepository.deleteAll();
        documentRepository.deleteAll();
        if (userRepository.findByUsername(TEST_USER_EMAIL).isEmpty()) {
            UserEntity testUser = new UserEntity();
            testUser.setUsername(TEST_USER_EMAIL);
            testUser.setEmail(TEST_USER_EMAIL);
            testUser.setPassword(TEST_USER_PASS);
            testUser.setActive(true);
            testUser.setFullName(TEST_USER_NAME);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setRole(ApplicationRole.USER);
            userRepository.save(testUser);
        }
        when(openAiClient.askAssistant(any(), any())).thenReturn(AI_STUB_RESPONSE);
        when(openAiClient.askAssistant(anyString(), anyString())).thenReturn(AI_STUB_RESPONSE);
        doReturn(List.of()).when(documentChunkRepository).findTopSimilar(anyLong(), any(float[].class), anyInt());
        session = new MockHttpSession();
        when(embeddingModel.embed(any(String.class))).thenAnswer(invocation -> DoubleStream.generate(Math::random)
                .limit(EMBEDDING_DIM).boxed().collect(Collectors.toList()));
        when(embeddingModel.embed(any(org.springframework.ai.document.Document.class))).thenAnswer(invocation -> DoubleStream.generate(Math::random)
                .limit(EMBEDDING_DIM).boxed().collect(Collectors.toList()));
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testCompleteUserWorkflow_FromUploadToAiResponse() throws Exception {
        InputStream pdfStream = new ClassPathResource(PDF_RESOURCE_PATH).getInputStream();
        doReturn(List.of(0.1, 0.2, 0.3)).when(embeddingModel).embed(anyString());
        MockMultipartFile pdfFile = new MockMultipartFile("file", PDF_RESOURCE_PATH, PDF_CONTENT_TYPE, pdfStream);
        mockMvc.perform(multipart("/chats/create").file(pdfFile).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/chat"));
        List<Document> documents = documentRepository.findAll();
        assertThat(documents).hasSize(1);
        Document uploadedDoc = documents.get(0);
        assertThat(uploadedDoc.getFilename()).isEqualTo(PDF_RESOURCE_PATH);
        List<ProcessingJob> jobs = processingJobRepository.findAll();
        assertThat(jobs).hasSize(1);
        ProcessingJob job = jobs.get(0);
        assertThat(job.getDocument().getId()).isEqualTo(uploadedDoc.getId());
        Long jobId = job.getId();
        await().atMost(AWAIT_TIMEOUT_30, TimeUnit.SECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            ProcessingJob updatedJob = processingJobRepository.findById(jobId).orElseThrow(() -> new AssertionError("Job не е намерен!"));
            assertThat(updatedJob.getStage()).isEqualTo(ProcessingJobStage.COMPLETED);
        });
        List<DocumentChunk> chunks = documentChunkRepository.findAll();
        assertThat(chunks).isNotEmpty();
        for (DocumentChunk chunk : chunks) {
            assertThat(chunk.getEmbedding()).isNotNull().isNotEmpty();
            assertThat(chunk.getDocument().getId()).isEqualTo(uploadedDoc.getId());
        }
        mockMvc.perform(post("/chat/send").param("message", USER_PROMPT).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/chat#bottom-anchor"));
        await().atMost(AWAIT_TIMEOUT_10, TimeUnit.SECONDS).pollInterval(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            List<Message> messages = messageRepository.findAll();
            assertThat(messages).hasSizeGreaterThanOrEqualTo(2);
        });
        List<Message> messages = messageRepository.findAll();
        Message userMessage = messages.stream().filter(m -> m.getRole() == MessageRole.USER).filter(m -> m.getContent().equals(USER_PROMPT)).findFirst().orElseThrow(() -> new AssertionError("USER съобщението не е намерено!"));
        assertThat(userMessage.getChat().getUser().getUsername()).isEqualTo(TEST_USER_EMAIL);
        Message assistantMessage = messages.stream().filter(m -> m.getRole() == MessageRole.ASSISTANT).findFirst().orElseThrow(() -> new AssertionError("ASSISTANT съобщението не е намерено!"));
        assertThat(assistantMessage.getContent()).isNotBlank().contains("AI");
        mockMvc.perform(get("/dashboard").session(session)).andExpect(status().isOk()).andExpect(view().name(DASHBOARD_VIEW_NAME)).andExpect(model().attributeExists(ChatController.ATTR_ALL_CHATS)).andExpect(model().attributeExists(ChatController.ATTR_CURRENT_USER));
        Long sessionChatId = (Long) session.getAttribute("currentChatId");
        assertThat(sessionChatId).isNotNull();
        Chat chatFromDb = chatRepository.findById(sessionChatId).orElseThrow(() -> new AssertionError("Chat не е намерен!"));
        assertThat(chatFromDb.getUser().getUsername()).isEqualTo(TEST_USER_EMAIL);
        assertThat(messageRepository.countByChatId(sessionChatId)).isGreaterThanOrEqualTo(2);
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testSendMessage_WithExistingChat_ShouldWork() throws Exception {
        Document document = new Document();
        document.setContent(KICKBOX_TEXT.getBytes());
        document.setFilename(KICKBOX_FILE_NAME);
        document.setMimeType(PDF_CONTENT_TYPE);
        document.setUploadedAt(LocalDateTime.now());
        document.setDocumentStatus(DocumentStatus.INDEXED);
        document = documentRepository.save(document);
        Chat chat = new Chat();
        chat.setUser(userRepository.findByUsername(TEST_USER_EMAIL).get());
        chat.setDocument(document);
        chat.setTitle(KICKBOX_CHAT_TITLE);
        chat.setLastMessageAt(LocalDateTime.now());
        chat.setOpenAiThreadId(KICKBOX_THREAD_ID);
        chat = chatRepository.save(chat);
        session.setAttribute(SESSION_CHAT_ID, chat.getId());
        when(openAiClient.askAssistant(anyString(), anyString())).thenReturn(KICKBOX_AI_ANSWER);
        mockMvc.perform(post("/chat/send").param("message", KICKBOX_USER_QUERY).with(csrf()).session(session)).andExpect(status().is3xxRedirection());
        assertThat(messageRepository.count()).isGreaterThanOrEqualTo(1);
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testSelectChat_ShouldSetSessionAttributeAndRedirect() throws Exception {
        Document doc = createAndSaveDocument();
        Chat chat = createAndSaveChat(doc);
        mockMvc.perform(get("/chats/select/" + chat.getId()).session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/chat"));
        assertThat((Long) session.getAttribute(SESSION_CHAT_ID)).isEqualTo(chat.getId());
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testViewChat_WithActiveSession_ShouldReturnChatView() throws Exception {
        Document doc = createAndSaveDocument();
        Chat chat = createAndSaveChat(doc);
        session.setAttribute(SESSION_CHAT_ID, chat.getId());
        mockMvc.perform(get("/chat").session(session)).andExpect(status().isOk()).andExpect(view().name(CHAT_VIEW_NAME)).andExpect(model().attributeExists(CHAT_MODEL_ATTR));
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testViewChat_WithoutActiveSession_ShouldRedirectToDashboard() throws Exception {
        mockMvc.perform(get("/chat").session(new MockHttpSession())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/dashboard"));
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testCreateChat_WithEmptyFile_ShouldRedirectWithError() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", EMPTY_FILE_NAME, PDF_CONTENT_TYPE, new byte[0]);
        mockMvc.perform(multipart("/chats/create").file(emptyFile).with(csrf()).session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/dashboard")).andExpect(flash().attributeExists(FLASH_ATTR_ERROR));
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testCreateChat_WhenServiceThrowsException_ShouldHandleError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", PDF_CONTENT_TYPE, "content".getBytes());
        doThrow(new RuntimeException(EXCEPTION_MSG)).when(chatService).startNewChat(any(), anyString());
        mockMvc.perform(multipart("/chats/create").file(file).with(csrf()).session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/dashboard")).andExpect(flash().attributeExists(FLASH_ATTR_ERROR)).andExpect(flash().attribute(FLASH_ATTR_ERROR, org.hamcrest.Matchers.containsString(EXCEPTION_MSG)));
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testSendMessage_WithInvalidParams_ShouldRedirectBack() throws Exception {
        mockMvc.perform(post("/chat/send").param("message", BLANK_MESSAGE).with(csrf()).session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/chat"));
        mockMvc.perform(post("/chat/send").param("message", VALID_MESSAGE_VAL).with(csrf()).session(new MockHttpSession())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/chat"));
    }
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = "USER")
    void testSendMessage_WhenAiClientFails_ShouldShowError() throws Exception {
        Document doc = createAndSaveDocument();
        Chat chat = createAndSaveChat(doc);
        session.setAttribute(SESSION_CHAT_ID, chat.getId());
        when(openAiClient.askAssistant(anyString(), anyString())).thenThrow(new RuntimeException(TIMEOUT_MSG));
        mockMvc.perform(post("/chat/send").param("message", TIMEOUT_QUERY).with(csrf()).session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/chat")).andExpect(flash().attributeExists(FLASH_ATTR_ERROR));
    }
    private Document createAndSaveDocument() {
        Document doc = new Document();
        doc.setContent("Content".getBytes());
        doc.setFilename("test.pdf");
        doc.setMimeType(PDF_CONTENT_TYPE);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setDocumentStatus(DocumentStatus.INDEXED);
        return documentRepository.save(doc);
    }
    private Chat createAndSaveChat(Document doc) {
        Chat chat = new Chat();
        chat.setUser(userRepository.findByUsername(TEST_USER_EMAIL).get());
        chat.setDocument(doc);
        chat.setTitle(DEFAULT_CHAT_TITLE);
        chat.setLastMessageAt(LocalDateTime.now());
        chat.setOpenAiThreadId(DEFAULT_THREAD_ID);
        return chatRepository.save(chat);
    }
}