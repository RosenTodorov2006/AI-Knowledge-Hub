package org.example.unit.controllers.rest;

import org.example.controllers.rest.ChatRestController;
import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.services.ChatService;
import org.example.services.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.Collections;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
@ExtendWith(MockitoExtension.class)
public class ChatRestControllerTest {
    private static final String URL_BASE = "/api/chats";
    private static final String URL_CREATE = "/api/chats/create";
    private static final String URL_SEND = "/api/chats/{id}/send";
    private static final String URL_DETAILS = "/api/chats/{id}";
    private static final String TEST_USER = "user@test.com";
    private static final Long CHAT_ID = 1L;
    private static final String CHAT_MSG = "Hello";
    private static final String ERR_EMPTY_FILE = "Empty file error";
    private static final String ERR_PROCESS_PREFIX = "Process error";
    private static final String RUNTIME_ERR = "System fail";
    private static final String JSON_PATH_ERROR = "$.error";
    private static final String JSON_PATH_ID = "$.id";
    private static final String JSON_REQUEST_BODY = "{\"message\":\"Hello\"}";
    private static final String FILE_PARAM = "file";
    private static final String FILE_NAME = "test.txt";
    private static final String CONTENT_TYPE = "text/plain";
    @Mock
    private DashboardService dashboardService;
    @Mock
    private ChatService chatService;
    @Mock
    private MessageSource messageSource;
    private MockMvc mockMvc;
    @BeforeEach
    public void setUp() {
        ChatRestController chatRestController = new ChatRestController(dashboardService, chatService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(chatRestController).build();
    }
    @Test
    public void testGetDashboardDataShouldReturnList() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(dashboardService.getAllChats(TEST_USER)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get(URL_BASE)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }
    @Test
    public void testCreateChatShouldReturnBadRequestWhenFileEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile(FILE_PARAM, "", CONTENT_TYPE, new byte[0]);
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(ERR_EMPTY_FILE);

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_CREATE)
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(JSON_PATH_ERROR).value(ERR_EMPTY_FILE));
    }
    @Test
    public void testCreateChatShouldReturnCreatedOnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(FILE_PARAM, FILE_NAME, CONTENT_TYPE, "data".getBytes());
        Principal principal = Mockito.mock(Principal.class);
        ChatViewDto dto = new ChatViewDto();
        dto.setId(CHAT_ID);

        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(chatService.startNewChat(any(), eq(TEST_USER))).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_CREATE)
                        .file(file)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath(JSON_PATH_ID).value(CHAT_ID));
    }
    @Test
    public void testCreateChatShouldReturnInternalErrorOnException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(FILE_PARAM, FILE_NAME, CONTENT_TYPE, "data".getBytes());
        Principal principal = Mockito.mock(Principal.class);

        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(chatService.startNewChat(any(), eq(TEST_USER))).thenThrow(new RuntimeException(RUNTIME_ERR));
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(ERR_PROCESS_PREFIX);

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_CREATE)
                        .file(file)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath(JSON_PATH_ERROR).value(containsString(ERR_PROCESS_PREFIX)));
    }
    @Test
    public void testSendMessageShouldReturnResponse() throws Exception {
        ChatResponseDto responseDto = new ChatResponseDto();
        Mockito.when(chatService.generateResponse(eq(CHAT_ID), eq(CHAT_MSG))).thenReturn(responseDto);

        mockMvc.perform(MockMvcRequestBuilders.post(URL_SEND, CHAT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REQUEST_BODY))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void testGetChatDetailsShouldReturnDto() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(chatService.getChatDetails(CHAT_ID, TEST_USER)).thenReturn(new ChatViewDto());

        mockMvc.perform(MockMvcRequestBuilders.get(URL_DETAILS, CHAT_ID)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
