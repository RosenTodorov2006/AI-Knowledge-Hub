package org.example.unit.controllers.rest;

import org.example.controllers.rest.SettingsRestController;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
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
public class SettingsRestControllerTest {
    private static final String URL_CHANGE_INFO = "/api/settings/changeInfo";
    private static final String URL_CHANGE_PASSWORD = "/api/settings/changeUserPassword";
    private static final String URL_DELETE = "/api/settings";
    private static final String TEST_USER = "user@test.com";
    private static final String SUCCESS_MSG = "Success update";
    private static final String ERR_MSG = "Validation failed";
    private static final String JSON_CHANGE_INFO = "{\"email\":\"new@test.com\",\"fullName\":\"New Name\"}";
    private static final String JSON_CHANGE_PW = "{\"password\":\"123456\",\"confirmPassword\":\"123456\"}";
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

        SettingsRestController controller = new SettingsRestController(userService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(mockValidator)
                .build();
    }
    @Test
    public void testChangeInfoSuccess() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(userService.getUserViewByEmail(anyString())).thenReturn(new UserViewDto());

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_INFO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CHANGE_INFO)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userService).changeProfileInfo(any(ChangeProfileDto.class), eq(TEST_USER));
    }
    @Test
    public void testChangeInfoValidationErrors() throws Exception {
        Mockito.doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "error.invalid", ERR_MSG);
            return null;
        }).when(mockValidator).validate(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_INFO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CHANGE_INFO))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value(ERR_MSG));
    }
    @Test
    public void testChangeUserPasswordSuccess() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(SUCCESS_MSG);

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CHANGE_PW)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SUCCESS_MSG));
    }
    @Test
    public void testChangeUserPasswordServiceFailure() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.doThrow(new RuntimeException("Wrong old password")).when(userService).changeUserPassword(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CHANGE_PW)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Wrong old password"));
    }
    @Test
    public void testDeleteAccountSuccess() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_USER);
        Mockito.when(messageSource.getMessage(anyString(), any(), any())).thenReturn(SUCCESS_MSG);

        mockMvc.perform(MockMvcRequestBuilders.delete(URL_DELETE)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(SUCCESS_MSG));
    }
}