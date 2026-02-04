package org.example.integration.web;
import org.example.integration.base.BaseIntegrationTest;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.ApplicationRole;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.example.controllers.web.SettingsController.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class SettingsIntegrationTest extends BaseIntegrationTest {
    private static final String USER_EMAIL = "user@test.bg";
    private static final String NEW_EMAIL = "new@test.bg";
    private static final String TEST_PASSWORD = "encodedPassword";
    private static final String TEST_USERNAME = "tester";
    private static final String TEST_NAME = "Original Name";
    private static final String UPDATED_NAME = "Updated Name";
    private static final String NEW_PASSWORD_VAL = "newPassword123";
    private static final String SETTINGS_VIEW = "settings";
    private static final String INVALID_EMAIL_VAL = "not-an-email";
    private static final String PARAM_FULL_NAME = "fullName";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_CONF_PASSWORD = "confirmPassword";
    private static final String EMPTY_STR = "";
    @Autowired
    private UserRepository userRepository;

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
                TEST_NAME
        );
        userRepository.save(user);
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testGetSettingsPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/settings"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(SETTINGS_VIEW))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_CURRENT_USER))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_CHANGE_PROFILE));
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testChangeProfileInfoSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/settings/changeInfo")
                        .param(PARAM_FULL_NAME, UPDATED_NAME)
                        .param(PARAM_EMAIL, NEW_EMAIL)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/settings?success=true"));

        Optional<UserEntity> updatedUser = userRepository.findByEmail(NEW_EMAIL);
        Assertions.assertTrue(updatedUser.isPresent());
        Assertions.assertEquals(UPDATED_NAME, updatedUser.get().getFullName());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testChangeProfileInfoValidationError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/settings/changeInfo")
                        .param(PARAM_FULL_NAME, EMPTY_STR)
                        .param(PARAM_EMAIL, INVALID_EMAIL_VAL)
                        .with(csrf()))

                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/settings"))
                .andExpect(MockMvcResultMatchers.flash().attribute(ATTR_INVALID_PROFILE, true))
                .andExpect(MockMvcResultMatchers.flash().attributeExists(BINDING_RESULT_PREFIX + ATTR_CHANGE_PROFILE));
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testChangeUserPasswordSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/settings/changeUserPassword")
                        .param(PARAM_PASSWORD, NEW_PASSWORD_VAL)
                        .param(PARAM_CONF_PASSWORD, NEW_PASSWORD_VAL)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/settings?pwSuccess=true"));
        UserEntity user = userRepository.findByEmail(USER_EMAIL).get();
        Assertions.assertNotEquals(TEST_PASSWORD, user.getPassword());
    }
    @Test
    @WithMockUser(username = USER_EMAIL)
    public void testDeleteAccount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/settings")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/?deactivated=true"));
        Optional<UserEntity> deletedUser = userRepository.findByEmail(USER_EMAIL);

        Assertions.assertTrue(deletedUser.isEmpty() || !deletedUser.get().isActive());

    }
}

