package org.example.integration.web;
import org.example.integration.base.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
public class PublicHomeIntegrationTest extends BaseIntegrationTest {
    private static final String INDEX_VIEW = "index";
    @Test
    public void testIndexReturnsIndexViewForAnonymousUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(INDEX_VIEW));
    }
    @Test
    @WithMockUser
    public void testIndexRedirectsToDashboardForAuthenticatedUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/dashboard"));
    }
}