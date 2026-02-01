package unit.controllers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.example.services.DashboardService;
import org.example.services.ChatService;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.example.controllers.web.ChatController;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import org.mockito.Mockito;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import java.util.Collections;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ChatControllerTest {
    private static final String URL_DASHBOARD = "/dashboard";
    private static final String URL_SELECT = "/chats/select/1";
    private static final String URL_CHAT = "/chat";
    private static final String URL_CREATE = "/chats/create";
    private static final String URL_SEND = "/chat/send";
    private static final String VIEW_DASHBOARD = "dashboard";
    private static final String VIEW_CHAT = "chat";
    private static final String TEST_EMAIL = "user@test.com";
    private static final Long TEST_ID = 1L;
    private static final String TEST_MSG = "Hello bot";
    private static final String ERR_MSG = "Error message";
    private static final String ATTR_CHATS = ChatController.ATTR_ALL_CHATS;
    private static final String ATTR_USER = ChatController.ATTR_CURRENT_USER;
    private static final String ATTR_CHAT = ChatController.ATTR_CHAT;
    private static final String ATTR_ERROR = ChatController.ATTR_ERROR;
    private static final String SESSION_CHAT_ID = ChatController.SESSION_CHAT_ID;
    @Mock private DashboardService dashboardService;
    @Mock private ChatService chatService;
    @Mock private UserService userService;
    @Mock private MessageSource messageSource;
    private MockMvc mockMvc;
    @BeforeEach
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        ChatController chatController = new ChatController(dashboardService, chatService, userService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setViewResolvers(viewResolver)
                .build();
    }
    @Test
    public void testDashboardShouldReturnView() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);
        Mockito.when(dashboardService.getAllChats(TEST_EMAIL)).thenReturn(Collections.emptyList());
        Mockito.when(userService.getUserViewByEmail(TEST_EMAIL)).thenReturn(new UserViewDto());

        mockMvc.perform(MockMvcRequestBuilders.get(URL_DASHBOARD).principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_DASHBOARD))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_CHATS, ATTR_USER));
    }
    @Test
    public void testSelectChatShouldSetSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_SELECT))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_CHAT))
                .andExpect(MockMvcResultMatchers.request().sessionAttribute(SESSION_CHAT_ID, TEST_ID));
    }
    @Test
    public void testViewChatShouldRedirectWhenNoId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_CHAT))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_DASHBOARD));
    }
    @Test
    public void testViewChatShouldReturnView() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);
        Mockito.when(chatService.getChatDetails(eq(TEST_ID), eq(TEST_EMAIL))).thenReturn(new ChatViewDto());

        mockMvc.perform(MockMvcRequestBuilders.get(URL_CHAT)
                        .sessionAttr(SESSION_CHAT_ID, TEST_ID)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_CHAT))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_CHAT));
    }
    @Test
    public void testProcessCreateChatWithEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(ERR_MSG);

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_CREATE).file(file))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_DASHBOARD))
                .andExpect(MockMvcResultMatchers.flash().attribute(ATTR_ERROR, ERR_MSG));
    }
    @Test
    public void testProcessCreateChatSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        Principal principal = Mockito.mock(Principal.class);
        ChatViewDto dto = new ChatViewDto();
        dto.setId(TEST_ID);

        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);
        Mockito.when(chatService.startNewChat(any(), eq(TEST_EMAIL))).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_CREATE).file(file).principal(principal))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_CHAT))
                .andExpect(MockMvcResultMatchers.request().sessionAttribute(SESSION_CHAT_ID, TEST_ID));
    }
    @Test
    public void testSendMessageSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL_SEND)
                        .sessionAttr(SESSION_CHAT_ID, TEST_ID)
                        .param("message", TEST_MSG))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_CHAT + ChatController.ANCHOR_BOTTOM));

        Mockito.verify(chatService).generateResponse(TEST_ID, TEST_MSG);
    }
    @Test
    public void testSendMessageInvalidInput() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL_SEND)
                        .sessionAttr(SESSION_CHAT_ID, TEST_ID)
                        .param("message", "  "))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_CHAT));

        Mockito.verifyNoInteractions(chatService);
    }
    @Test
    public void testSendMessageFailure() throws Exception {
        Mockito.when(chatService.generateResponse(any(), any())).thenThrow(new RuntimeException());
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(ERR_MSG);

        mockMvc.perform(MockMvcRequestBuilders.post(URL_SEND)
                        .sessionAttr(SESSION_CHAT_ID, TEST_ID)
                        .param("message", TEST_MSG))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(URL_CHAT))
                .andExpect(MockMvcResultMatchers.flash().attribute(ATTR_ERROR, ERR_MSG));
    }
}
