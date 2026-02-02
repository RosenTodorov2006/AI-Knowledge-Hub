package org.example.unit.controllers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.example.services.AdminService;
import org.example.services.UserService;
import org.example.controllers.web.AdminController;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import org.mockito.Mockito;
import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.example.models.dtos.exportDtos.UserViewDto;
import java.util.Collections;
import java.util.List;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {
    private static final String URL_MONITOR = "/admin/monitor";
    private static final String VIEW_ADMIN_MONITOR = "admin-monitor";
    private static final String USER_EMAIL = "admin@example.com";
    private static final String ATTR_JOBS = AdminController.ATTR_JOBS;
    private static final String ATTR_STATS = AdminController.ATTR_STATS;
    private static final String ATTR_CURRENT_USER = AdminController.ATTR_CURRENT_USER;
    private static final long MOCK_COUNT = 0L;
    private static final double MOCK_RATIO = 0.0;
    private static final String MOCK_DESC = "test";
    @Mock
    private AdminService adminService;
    @Mock
    private UserService userService;
    private MockMvc mockMvc;
    @BeforeEach
    public void setUp() {
        AdminController adminController = new AdminController(adminService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }
    @Test
    public void testAdminMonitorShouldReturnCorrectViewAndModel() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn(USER_EMAIL);

        List<Object> mockJobs = Collections.emptyList();
        AdminStatsDto mockStats = new AdminStatsDto(MOCK_COUNT, MOCK_RATIO, MOCK_DESC);
        UserViewDto mockUser = new UserViewDto();

        Mockito.when(adminService.getFailedJobs()).thenAnswer(inv -> mockJobs);
        Mockito.when(adminService.getSystemStats()).thenReturn(mockStats);
        Mockito.when(userService.getUserViewByEmail(USER_EMAIL)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_MONITOR)
                        .principal(mockPrincipal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name(VIEW_ADMIN_MONITOR))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_JOBS, mockJobs))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_STATS, mockStats))
                .andExpect(MockMvcResultMatchers.model().attribute(ATTR_CURRENT_USER, mockUser));

        Mockito.verify(adminService).getFailedJobs();
        Mockito.verify(adminService).getSystemStats();
        Mockito.verify(userService).getUserViewByEmail(USER_EMAIL);
    }
}