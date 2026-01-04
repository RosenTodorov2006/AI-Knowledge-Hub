package org.example.web.restControllersForTests;

import org.example.models.dtos.exportDtos.ChatDto;
import org.example.services.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/test/dashboard")
public class DashboardRestController {
    private final DashboardService dashboardService;

    public DashboardRestController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping()
    public List<ChatDto> getDashboardData(Principal principal) {
        String email = principal.getName();
        return this.dashboardService.getAllChats(email);
    }
}
