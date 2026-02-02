package org.example.unit.controllers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.example.controllers.web.PublicHomeController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.Authentication;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
public class PublicHomeControllerTest {
    private static final String URL_INDEX = "/";
    private static final String VIEW_INDEX = "index";
    private static final String REDIRECT_PATH = "/dashboard";
    private MockMvc mockMvc;
    @BeforeEach
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");

        PublicHomeController publicHomeController = new PublicHomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(publicHomeController)
                .setViewResolvers(viewResolver)
                .build();
    }
    @Test
    public void testIndexShouldReturnIndexWhenNoPrincipal() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL_INDEX))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_INDEX));
    }
    @Test
    public void testIndexShouldRedirectWhenFullyAuthenticated() throws Exception {
        Authentication mockAuth = Mockito.mock(Authentication.class);
        Mockito.when(mockAuth.isAuthenticated()).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_INDEX).principal(mockAuth))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(REDIRECT_PATH));
    }
    @Test
    public void testIndexShouldReturnIndexWhenAnonymous() throws Exception {
        AnonymousAuthenticationToken mockAnonymous = Mockito.mock(AnonymousAuthenticationToken.class);
        Mockito.when(mockAnonymous.isAuthenticated()).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_INDEX).principal(mockAnonymous))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_INDEX));
    }
    @Test
    public void testIndexShouldReturnIndexWhenNotAuthenticated() throws Exception {
        Authentication mockAuth = Mockito.mock(Authentication.class);
        Mockito.when(mockAuth.isAuthenticated()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_INDEX).principal(mockAuth))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_INDEX));
    }
}
