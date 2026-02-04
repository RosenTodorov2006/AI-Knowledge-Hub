package org.example.integration.rest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.integration.base.BaseIntegrationTest;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.repositories.UserRepository;
import org.example.controllers.rest.UserRestController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
@AutoConfigureMockMvc
public class UserRestControllerIntegrationTest extends BaseIntegrationTest {
    private static final String API_PREFIX = "/api/test/auth";
    private static final String TEST_USERNAME = "rest_user";
    private static final String TEST_EMAIL = "rest@test.bg";
    private static final String TEST_FULL_NAME = "Rest Tester";
    private static final String TEST_PASSWORD = "password123";
    private static final String INVALID_EMAIL = "not-an-email";
    private static final String LOGGED_USER = "logged_user";
    private static final String EMPTY_STRING = "";
    private static final String JSON_ROOT = "$.";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    public void testRestRegistration_Success() throws Exception {
        RegisterSeedDto dto = new RegisterSeedDto();
        dto.setUsername(TEST_USERNAME);
        dto.setEmail(TEST_EMAIL);
        dto.setFullName(TEST_FULL_NAME);
        dto.setPassword(TEST_PASSWORD);
        dto.setConfirmPassword(TEST_PASSWORD);
        mockMvc.perform(post(API_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_ROOT + UserRestController.JSON_KEY_MESSAGE).exists());
        Assertions.assertTrue(userRepository.findByUsername(TEST_USERNAME).isPresent());
    }
    @Test
    public void testRestRegistration_ValidationFailure() throws Exception {
        RegisterSeedDto invalidDto = new RegisterSeedDto();
        invalidDto.setUsername(EMPTY_STRING);
        invalidDto.setEmail(INVALID_EMAIL);
        mockMvc.perform(post(API_PREFIX + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_ROOT + UserRestController.JSON_KEY_ERRORS).isArray());
    }
    @Test
    @WithMockUser(username = LOGGED_USER)
    public void testGetCurrentUser_LoggedIn() throws Exception {
        mockMvc.perform(get(API_PREFIX + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_ROOT + UserRestController.JSON_KEY_USERNAME).value(LOGGED_USER));
    }
    @Test
    public void testGetCurrentUser_Anonymous() throws Exception {
        mockMvc.perform(get(API_PREFIX + "/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(JSON_ROOT + UserRestController.JSON_KEY_MESSAGE).exists());
    }
}