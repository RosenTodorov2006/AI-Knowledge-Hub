package org.example.unit.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.example.models.entities.enums.DocumentStatus;
import java.time.LocalDateTime;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.example.services.UserService;
import org.example.services.ChatService;
import org.example.services.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.models.entities.UserEntity;
import org.example.models.entities.Document;
import org.example.models.entities.Chat;
import org.example.models.dtos.exportDtos.ChatDto;
import org.mockito.Mockito;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.Assertions;
@ExtendWith(MockitoExtension.class)
public class DashboardServiceImplTest {
    private static final String TEST_GMAIL = "test@example.com";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_FILENAME = "document.pdf";
    private static final DocumentStatus TEST_STATUS = DocumentStatus.PROCESSING;
    private static final LocalDateTime TEST_UPLOAD_TIME = LocalDateTime.of(2026, 1, 31, 12, 0);
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserService userService;
    @Mock
    private ChatService chatService;
    private DashboardServiceImpl dashboardService;
    @BeforeEach
    public void setUp() {
        dashboardService = new DashboardServiceImpl(modelMapper, userService, chatService);
    }
    @Test
    public void testGetAllChatsShouldReturnCorrectMappedList() {
        UserEntity user = new UserEntity();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_GMAIL);

        Document doc = new Document();
        doc.setFilename(TEST_FILENAME);
        doc.setDocumentStatus(TEST_STATUS);
        doc.setUploadedAt(TEST_UPLOAD_TIME);

        Chat chat = new Chat();
        chat.setDocument(doc);

        ChatDto chatDto = new ChatDto();

        Mockito.when(userService.findUserByEmail(TEST_GMAIL)).thenReturn(user);
        Mockito.when(chatService.findAllChatsByUserEntityId(TEST_USER_ID)).thenReturn(List.of(chat));
        Mockito.when(modelMapper.map(any(Chat.class), eq(ChatDto.class))).thenReturn(chatDto);

        List<ChatDto> result = dashboardService.getAllChats(TEST_GMAIL);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        ChatDto mappedDto = result.get(0);
        Assertions.assertEquals(TEST_FILENAME, mappedDto.getFilename());
        Assertions.assertEquals(TEST_STATUS, mappedDto.getDocumentStatus());
        Assertions.assertEquals(TEST_UPLOAD_TIME, mappedDto.getUploadedAt());

        Mockito.verify(userService).findUserByEmail(TEST_GMAIL);
        Mockito.verify(chatService).findAllChatsByUserEntityId(TEST_USER_ID);
    }
    @Test
    public void testGetAllChatsShouldReturnEmptyListWhenUserHasNoChats() {
        UserEntity user = new UserEntity();
        user.setId(TEST_USER_ID);

        Mockito.when(userService.findUserByEmail(TEST_GMAIL)).thenReturn(user);
        Mockito.when(chatService.findAllChatsByUserEntityId(TEST_USER_ID)).thenReturn(List.of());

        List<ChatDto> result = dashboardService.getAllChats(TEST_GMAIL);

        Assertions.assertTrue(result.isEmpty());
    }
}