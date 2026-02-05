package org.example.integration.rest;
import org.example.integration.base.BaseIntegrationTest;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.ApplicationRole;
import org.example.repositories.UserRepository;
import org.example.controllers.rest.AdminRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
@AutoConfigureMockMvc
public class AdminRestControllerIntegrationTest extends BaseIntegrationTest {
    private static final String API_BASE = "/api/admin";
    private static final String ADMIN_EMAIL = "admin@test.bg";
    private static final String USER_EMAIL = "user@test.bg";
    private static final String TEST_PASS = "pass";
    private static final String TEST_ADMIN_USERNAME = "admin_user";
    private static final String TEST_ADMIN_FULL_NAME = "Admin Name";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        UserEntity admin = new UserEntity(
                ADMIN_EMAIL,
                TEST_PASS,
                TEST_ADMIN_USERNAME,
                ApplicationRole.ADMIN,
                true,
                LocalDateTime.now(),
                TEST_ADMIN_FULL_NAME
        );
        userRepository.save(admin);
    }
    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {ROLE_ADMIN})
    public void testGetStats_AsAdmin_Success() throws Exception {
        mockMvc.perform(get(API_BASE + "/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE_JSON));
    }
    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {ROLE_ADMIN})
    public void testGetFailedJobs_AsAdmin_Success() throws Exception {
        mockMvc.perform(get(API_BASE + "/failed-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {ROLE_ADMIN})
    public void testGetFullMonitorData_AsAdmin_Success() throws Exception {
        mockMvc.perform(get(API_BASE + "/monitor-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + AdminRestController.JSON_KEY_STATS).exists())
                .andExpect(jsonPath("$." + AdminRestController.JSON_KEY_FAILED_JOBS).isArray());
    }
    @Test
    @WithMockUser(username = USER_EMAIL, roles = {ROLE_USER})
    public void testAdminApi_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get(API_BASE + "/stats"))
                .andExpect(status().isForbidden());
    }
    @Test
    public void testAdminApi_AsAnonymous_Unauthorized() throws Exception {
        mockMvc.perform(get(API_BASE + "/monitor-data"))
                .andExpect(status().isUnauthorized());
    }
}