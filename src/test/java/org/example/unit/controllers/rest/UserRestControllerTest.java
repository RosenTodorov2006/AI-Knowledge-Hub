package org.example.unit.controllers.rest;

import org.example.controllers.rest.UserRestController;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.security.Principal;
import static org.mockito.ArgumentMatchers.*;
@ExtendWith(MockitoExtension.class)
public class UserRestControllerTest {
    private static final String URL_REGISTER = "/api/test/auth/register";
    private static final String URL_ME = "/api/test/auth/me";
    private static final String TEST_USER = "test_user";
    private static final String SUCCESS_MSG = "Registration successful";
    private static final String UNAUTHORIZED_MSG = "Not logged in";
    private static final String ERR_VALIDATION = "Invalid email format";
    private static final String JSON_REGISTER = "{\"username\":\"test\",\"email\":\"test@test.com\",\"password\":\"12345\"}";
    @Mock
    private UserService userService;
    @Mock
    private MessageSource messageSource;
    private MockMvc mockMvc;
    private Validator mockValidator;
    @BeforeEach
    public void setUp() {
        mockValidator = Mockito.mock(Validator.class);
        Mockito.lenient().when(mockValidator.supports(any())).thenReturn(true);

        UserRestController controller = new UserRestController(userService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(mockValidator)
                .build();
    }
    @Test
    public void testRegisterSuccess() throws Exception {
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(SUCCESS_MSG);

        mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REGISTER))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SUCCESS_MSG));

        Mockito.verify(userService).register(any(RegisterSeedDto.class));
    }
    @Test
    public void testRegisterValidationErrors() throws Exception {
        Mockito.doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "invalid", ERR_VALIDATION);
            return null;
        }).when(mockValidator).validate(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REGISTER))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(ERR_VALIDATION));
    }
    @Test
    public void testRegisterServiceException() throws Exception {
        Mockito.doThrow(new RuntimeException("Email already exists")).when(userService).register(any());

        mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REGISTER))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Email already exists"));
    }

    @Test
    public void testGetCurrentUserWhenLoggedIn() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_ME)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(TEST_USER));
    }

    @Test
    public void testGetCurrentUserWhenNotLoggedIn() throws Exception {
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(UNAUTHORIZED_MSG);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_ME))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(UNAUTHORIZED_MSG));
    }
}
