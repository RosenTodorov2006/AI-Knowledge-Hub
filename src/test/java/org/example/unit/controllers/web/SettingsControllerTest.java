package org.example.unit.controllers.web;

import org.example.controllers.web.SettingsController;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import java.security.Principal;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
@ExtendWith(MockitoExtension.class)
public class SettingsControllerTest {
    private static final String URL_SETTINGS = "/settings";
    private static final String URL_CHANGE_INFO = "/settings/changeInfo";
    private static final String URL_CHANGE_PASSWORD = "/settings/changeUserPassword";
    private static final String VIEW_SETTINGS = "settings";
    private static final String REDIRECT_SETTINGS = "/settings";
    private static final String REDIRECT_SETTINGS_SUCCESS = "/settings?success=true";
    private static final String REDIRECT_SETTINGS_PW_SUCCESS = "/settings?pwSuccess=true";
    private static final String REDIRECT_INDEX_DEACTIVATED = "/?deactivated=true";
    private static final String TEST_EMAIL = "user@test.com";
    private static final String NEW_EMAIL = "new@test.com";
    private static final String FULL_NAME = "John Doe";
    private static final String ATTR_PROFILE = SettingsController.ATTR_CHANGE_PROFILE;
    private static final String ATTR_PASSWORD = SettingsController.ATTR_CHANGE_PASSWORD;
    private static final String ATTR_USER = SettingsController.ATTR_CURRENT_USER;
    private static final String ATTR_INVALID_PASSWORD = SettingsController.ATTR_INVALID_PASSWORD;
    @Mock
    private UserService userService;
    private MockMvc mockMvc;
    private Validator mockValidator;
    @BeforeEach
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");

        // 2. Инициализираме полето
        mockValidator = Mockito.mock(Validator.class);
        Mockito.lenient().when(mockValidator.supports(Mockito.any())).thenReturn(true);

        SettingsController settingsController = new SettingsController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(settingsController)
                .setViewResolvers(viewResolver)
                .setValidator(mockValidator)
                .build();
    }
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    public void testSettingsShouldPopulateModel() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);
        Mockito.when(userService.getChangeProfileDto(TEST_EMAIL)).thenReturn(new ChangeProfileDto());
        Mockito.when(userService.getUserViewByEmail(TEST_EMAIL)).thenReturn(new UserViewDto());

        mockMvc.perform(MockMvcRequestBuilders.get(URL_SETTINGS).principal(principal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_SETTINGS))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_PROFILE, ATTR_PASSWORD, ATTR_USER));
    }
    @Test
    public void testChangeInfoSuccessWithEmailUpdate() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_INFO)
                        .param("email", NEW_EMAIL)
                        .param("fullName", FULL_NAME)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(REDIRECT_SETTINGS_SUCCESS));

        Mockito.verify(userService).changeProfileInfo(any(ChangeProfileDto.class), eq(TEST_EMAIL));
        Mockito.verify(securityContext).setAuthentication(any());
    }
    @Test
    public void testDisableAccountSuccess() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.delete(URL_SETTINGS)
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(REDIRECT_INDEX_DEACTIVATED));

        Mockito.verify(userService).deleteUser(TEST_EMAIL);
    }
    @Test
    public void testChangeUserPasswordValidationErrors() throws Exception {
        Mockito.doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("password", "error.length");
            return null;
        }).when(mockValidator).validate(Mockito.any(), Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_PASSWORD)
                        .param("password", "1")
                        .param("confirmPassword", "1"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(REDIRECT_SETTINGS))
                .andExpect(MockMvcResultMatchers.flash().attribute(ATTR_INVALID_PASSWORD, true));

        Mockito.verifyNoInteractions(userService);
    }
    @Test
    public void testChangeUserPasswordSuccess() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(TEST_EMAIL);

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(auth.getCredentials()).thenReturn("credentials");
        Mockito.when(auth.getAuthorities()).thenReturn(Collections.emptyList());
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.post(URL_CHANGE_PASSWORD)
                        .param("password", "newPassword123")
                        .param("confirmPassword", "newPassword123")
                        .principal(principal))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(REDIRECT_SETTINGS_PW_SUCCESS));

        Mockito.verify(userService).changeUserPassword(any(ChangeUserPasswordDto.class), eq(TEST_EMAIL));
        Mockito.verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
    }
}