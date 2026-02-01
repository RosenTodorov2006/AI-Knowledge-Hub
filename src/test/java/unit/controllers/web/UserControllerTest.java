package unit.controllers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.example.controllers.web.UserController;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.mockito.Mockito;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.authentication.DisabledException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    private static final String URL_REGISTER = "/register";
    private static final String URL_LOGIN = "/login";
    private static final String URL_LOGIN_ERROR = "/login-error";
    private static final String VIEW_REGISTER = "register";
    private static final String VIEW_LOGIN = "login";
    private static final String REDIRECT_LOGIN = "/login";
    private static final String ATTR_REGISTER = UserController.ATTR_REGISTER;
    private static final String ATTR_LOGIN = UserController.ATTR_LOGIN;
    private static final String ATTR_INVALID = UserController.ATTR_INVALID_DATA;
    private static final String ATTR_ERROR = UserController.ATTR_ERROR_MSG;
    private static final String SECURITY_EX_KEY = UserController.SPRING_SECURITY_LAST_EXCEPTION;

    private static final String ERR_INVALID = "Invalid credentials";
    private static final String ERR_DISABLED = "Account disabled";
    @Mock
    private UserService userService;
    @Mock
    private MessageSource messageSource;
    private MockMvc mockMvc;
    @BeforeEach
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");

        Validator mockValidator = Mockito.mock(Validator.class);
        Mockito.lenient().when(mockValidator.supports(any())).thenReturn(true);

        UserController userController = new UserController(userService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setViewResolvers(viewResolver)
                .setValidator(mockValidator)
                .build();
    }
    @Test
    public void testRegisterGetShouldReturnView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_REGISTER))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_REGISTER))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_REGISTER));
    }
    @Test
    public void testRegisterPostSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                        .param("username", "testUser")
                        .param("email", "test@test.com")
                        .param("password", "12345")
                        .param("confirmPassword", "12345"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(REDIRECT_LOGIN));

        Mockito.verify(userService).register(any(RegisterSeedDto.class));
    }
    @Test
    public void testLoginGetShouldReturnView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_LOGIN))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_LOGIN))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_INVALID, false))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_LOGIN));
    }
    @Test
    public void testLoginErrorWithGenericException() throws Exception {
        Mockito.when(messageSource.getMessage(eq("error.login.invalid"), isNull(), any())).thenReturn(ERR_INVALID);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_LOGIN_ERROR))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_LOGIN))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_INVALID, true))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_ERROR, ERR_INVALID));
    }
    @Test
    public void testLoginErrorWithDisabledException() throws Exception {
        Mockito.when(messageSource.getMessage(eq("error.login.disabled"), isNull(), any())).thenReturn(ERR_DISABLED);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_LOGIN_ERROR)
                        .sessionAttr(SECURITY_EX_KEY, new DisabledException("Disabled")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_LOGIN))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_INVALID, true))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_ERROR, ERR_DISABLED));
    }
}
