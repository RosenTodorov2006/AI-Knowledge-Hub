package org.example.integration.web;

import org.example.integration.base.BaseIntegrationTest;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.ApplicationRole;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.time.LocalDateTime;

public class AdminIntegrationTest extends BaseIntegrationTest {
    private static final String ADMIN_EMAIL = "admin@test.bg";
    private static final String USER_EMAIL = "user@test.bg";
    private static final String TEST_PASSWORD = "pass";
    private static final String TEST_USERNAME = "admin_user";
    private static final String TEST_FULL_NAME = "Admin Name";

    private static final String ADMIN_URL = "/admin/monitor";
    private static final String ADMIN_VIEW = "admin-monitor";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    @Autowired
    private UserRepository userRepository;
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity(
                ADMIN_EMAIL,
                TEST_PASSWORD,
                TEST_USERNAME,
                ApplicationRole.ADMIN,
                true,
                LocalDateTime.now(),
                TEST_FULL_NAME
        );
        userRepository.save(admin);
    }
    @Test
    @WithMockUser(username = ADMIN_EMAIL, roles = {ROLE_ADMIN})
    public void testAdminMonitorAsAdminSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ADMIN_URL))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(ADMIN_VIEW));
    }
    @Test
    @WithMockUser(username = USER_EMAIL, roles = {ROLE_USER})
    public void testAdminMonitorAsUserForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ADMIN_URL))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    @Test
    public void testAdminMonitorAsAnonymousIsUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ADMIN_URL))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}