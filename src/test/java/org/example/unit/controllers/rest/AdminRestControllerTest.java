package org.example.unit.controllers.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.example.services.AdminService;
import org.example.controllers.rest.AdminRestController;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mockito;
import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.example.models.dtos.exportDtos.ProcessingJobDto;
import java.util.List;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
@ExtendWith(MockitoExtension.class)
public class AdminRestControllerTest {
    private static final String URL_STATS = "/api/admin/stats";
    private static final String URL_FAILED_JOBS = "/api/admin/failed-jobs";
    private static final String URL_MONITOR_DATA = "/api/admin/monitor-data";
    private static final long MOCK_COUNT = 10L;
    private static final double MOCK_RATIO = 0.85;
    private static final String MOCK_DESC = "System Active";
    @Mock
    private AdminService adminService;
    private MockMvc mockMvc;
    @BeforeEach
    public void setUp() {
        AdminRestController adminRestController = new AdminRestController(adminService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminRestController).build();
    }
    @Test
    public void testGetStatsShouldReturnDto() throws Exception {
        AdminStatsDto mockStats = new AdminStatsDto(MOCK_COUNT, MOCK_RATIO, MOCK_DESC);
        Mockito.when(adminService.getSystemStats()).thenReturn(mockStats);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_STATS))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isMap());
    }
    @Test
    public void testGetFailedJobsShouldReturnList() throws Exception {
        List<ProcessingJobDto> mockJobs = List.of(new ProcessingJobDto());
        Mockito.when(adminService.getFailedJobs()).thenReturn(mockJobs);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_FAILED_JOBS))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
    }
    @Test
    public void testGetFullMonitorDataShouldReturnMap() throws Exception {
        AdminStatsDto mockStats = new AdminStatsDto(MOCK_COUNT, MOCK_RATIO, MOCK_DESC);
        List<ProcessingJobDto> mockJobs = List.of(new ProcessingJobDto());

        Mockito.when(adminService.getSystemStats()).thenReturn(mockStats);
        Mockito.when(adminService.getFailedJobs()).thenReturn(mockJobs);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_MONITOR_DATA))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.stats").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.failedJobs").isArray());
    }
}
