package org.example.integration.web;
import org.example.integration.base.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import static org.example.controllers.web.GlobalExceptionHandler.*;
@Import(GlobalExceptionHandlerIntegrationTest.ExceptionTriggerController.class)
public class GlobalExceptionHandlerIntegrationTest extends BaseIntegrationTest {
    private static final String MSG_NOT_FOUND = "error.document.not.found";
    private static final String MSG_UNEXPECTED = "Unexpected error";
    private static final String VIEW_ERROR = "error";
    private static final int CODE_404 = 404;
    private static final int CODE_500 = 500;
    @Controller
    static class ExceptionTriggerController {
        @GetMapping("/test/response-status-exception")
        public void triggerResponseStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_NOT_FOUND);
        }
        @GetMapping("/test/general-exception")
        public void triggerGeneral() throws Exception {
            throw new Exception(MSG_UNEXPECTED);
        }
    }
    @Test
    @WithMockUser
    public void testHandleResponseStatusException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/response-status-exception"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_ERROR))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_STATUS, CODE_404))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_ERROR))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_MESSAGE));
    }
    @Test
    @WithMockUser
    public void testHandleGeneralException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/general-exception"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_ERROR))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_STATUS, CODE_500))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_ERROR))
                .andExpect(MockMvcResultMatchers.model().attributeExists(ATTR_MESSAGE));
    }
}