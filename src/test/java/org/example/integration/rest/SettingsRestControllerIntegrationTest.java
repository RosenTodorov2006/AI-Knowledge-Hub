package org.example.integration.rest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.integration.base.BaseIntegrationTest;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.ApplicationRole;
import org.example.repositories.UserRepository;
import org.example.controllers.rest.SettingsRestController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
@AutoConfigureMockMvc
public class SettingsRestControllerIntegrationTest extends BaseIntegrationTest {
    private static final String API_BASE = "/api/settings";
    private static final String USER_EMAIL = "user@test.bg";
    private static final String TEST_PASSWORD = "encodedPassword";
    private static final String TEST_USERNAME = "tester";
    private static final String TEST_FULL_NAME = "Original Name";
    private static final String UPDATED_FULL_NAME = "Updated REST Name";
    private static final String UPDATED_EMAIL = "rest_new@test.bg";
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String NEW_PASSWORD = "newPass123";
    private static final String EMPTY_STRING = "";
    private static final String JSON_PATH_EMAIL = "$.email";
    private static final String JSON_PATH_FULL_NAME = "$.fullName";
    private static final String JSON_PREFIX = "$.";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        UserEntity user = new UserEntity(
                USER_EMAIL,
                TEST_PASSWORD,
                TEST_USERNAME,
                ApplicationRole.USER,
                true,
                LocalDateTime.now(),
                TEST_FULL_NAME
        );
        userRepository.save(user);
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testRestChangeInfo_Success() throws Exception {
        ChangeProfileDto dto = new ChangeProfileDto();
        dto.setFullName(UPDATED_FULL_NAME);
        dto.setEmail(UPDATED_EMAIL);
        mockMvc.perform(post(API_BASE + "/changeInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PATH_EMAIL).value(UPDATED_EMAIL))
                .andExpect(jsonPath(JSON_PATH_FULL_NAME).value(UPDATED_FULL_NAME));
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testRestChangeInfo_ValidationError() throws Exception {
        ChangeProfileDto invalidDto = new ChangeProfileDto();
        invalidDto.setFullName(EMPTY_STRING);
        invalidDto.setEmail(INVALID_EMAIL);
        mockMvc.perform(post(API_BASE + "/changeInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_PREFIX + SettingsRestController.JSON_KEY_ERRORS).isArray());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testRestChangePassword_Success() throws Exception {
        ChangeUserPasswordDto dto = new ChangeUserPasswordDto();
        dto.setPassword(NEW_PASSWORD);
        dto.setConfirmPassword(NEW_PASSWORD);
        mockMvc.perform(post(API_BASE + "/changeUserPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PREFIX + SettingsRestController.JSON_KEY_MESSAGE).exists());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testRestDeleteAccount_Success() throws Exception {
        mockMvc.perform(delete(API_BASE)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_PREFIX + SettingsRestController.JSON_KEY_MESSAGE).exists());
        Optional<UserEntity> deletedUser = userRepository.findByEmail(USER_EMAIL);
        Assertions.assertTrue(deletedUser.isEmpty() || !deletedUser.get().isActive());
    }
}