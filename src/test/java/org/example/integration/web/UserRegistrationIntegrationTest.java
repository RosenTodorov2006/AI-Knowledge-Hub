package org.example.integration.web;

import org.example.integration.base.BaseIntegrationTest;
import org.example.models.entities.UserEntity;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.Optional;
import static org.example.controllers.web.UserController.*;
public class UserRegistrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    private static final String TEST_EMAIL = "integration@test.bg";
    private static final String TEST_USERNAME = "tester_int";
    private static final String TEST_FULL_NAME = "Integration Tester";
    private static final String TEST_PASSWORD = "topsecret123";
    private static final String VIEW_REGISTER = "register";
    private static final String VIEW_LOGIN = "login";

    @Test
    public void testRegistrationSavesUserInDatabase() throws Exception {
        Assertions.assertFalse(userRepository.findByEmail(TEST_EMAIL).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("username", TEST_USERNAME)
                        .param("email", TEST_EMAIL)
                        .param("fullName", TEST_FULL_NAME)
                        .param("password", TEST_PASSWORD)
                        .param("confirmPassword", TEST_PASSWORD))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"));

        Optional<UserEntity> savedUserOpt = userRepository.findByEmail(TEST_EMAIL);
        Assertions.assertTrue(savedUserOpt.isPresent());

        UserEntity savedUser = savedUserOpt.get();
        Assertions.assertEquals(TEST_USERNAME, savedUser.getUsername());
    }

    @Test
    public void testGetRegisterPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_REGISTER))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_REGISTER));
    }

    @Test
    public void testPostRegisterValidationFailure() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .param("username", "")
                        .param("email", "not-an-email")
                        .param("password", "1"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/register"))
                .andExpect(MockMvcResultMatchers.flash().attributeExists(ATTR_REGISTER))
                .andExpect(MockMvcResultMatchers.flash().attributeExists(BINDING_RESULT_PREFIX + ATTR_REGISTER));
    }

    @Test
    public void testGetLoginPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_LOGIN))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_INVALID_DATA, false))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_LOGIN));
    }

    @Test
    public void testLoginErrorGeneric() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login-error"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_LOGIN))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_INVALID_DATA, true))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_ERROR_MSG));
    }

    @Test
    public void testLoginErrorDisabledAccount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login-error")
                        .sessionAttr(SPRING_SECURITY_LAST_EXCEPTION, new DisabledException("Disabled")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_LOGIN))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_ERROR_MSG));
    }
}