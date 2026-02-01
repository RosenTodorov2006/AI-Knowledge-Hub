package unit.controllers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.example.controllers.web.GlobalExceptionHandler;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    private static final String URL_TRIGGER_RESPONSE_STATUS = "/trigger-response-status";
    private static final String URL_TRIGGER_RUNTIME = "/trigger-runtime";
    private static final String VIEW_ERROR = "error";
    private static final String TEST_REASON = "error.test.notfound";
    private static final String TRANSLATED_MSG = "Translated Not Found Message";
    private static final String INTERNAL_ERR_TITLE = "Internal Server Error";
    private static final String UNEXPECTED_ERR_MSG = "An unexpected error occurred";
    private static final String ATTR_STATUS = GlobalExceptionHandler.ATTR_STATUS;
    private static final String ATTR_ERROR = GlobalExceptionHandler.ATTR_ERROR;
    private static final String ATTR_MESSAGE = GlobalExceptionHandler.ATTR_MESSAGE;
    @Mock
    private MessageSource messageSource;
    private MockMvc mockMvc;
    @Controller
    public static class ExceptionTriggerController {
        @GetMapping(URL_TRIGGER_RESPONSE_STATUS)
        public void triggerResponseStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TEST_REASON);
        }

        @GetMapping(URL_TRIGGER_RUNTIME)
        public void triggerRuntime() {
            throw new RuntimeException("Unexpected panic");
        }
    }
    @BeforeEach
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler(messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTriggerController())
                .setControllerAdvice(globalExceptionHandler)
                .setViewResolvers(viewResolver)
                .build();
    }
    @Test
    public void testHandleResponseStatusException() throws Exception {
        Mockito.when(messageSource.getMessage(eq(TEST_REASON), any(), eq(TEST_REASON), any()))
                .thenReturn(TRANSLATED_MSG);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_TRIGGER_RESPONSE_STATUS))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_ERROR))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_STATUS, 404))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_MESSAGE, TRANSLATED_MSG));
    }
    @Test
    public void testHandleGeneralException() throws Exception {
        Mockito.when(messageSource.getMessage(eq("error.internal.server"), any(), any()))
                .thenReturn(INTERNAL_ERR_TITLE);
        Mockito.when(messageSource.getMessage(eq("error.unexpected"), any(), any()))
                .thenReturn(UNEXPECTED_ERR_MSG);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_TRIGGER_RUNTIME))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_ERROR))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_STATUS, 500))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_ERROR, INTERNAL_ERR_TITLE))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_MESSAGE, UNEXPECTED_ERR_MSG));
    }
}
