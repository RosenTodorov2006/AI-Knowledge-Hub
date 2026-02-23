package org.example.controllers.rest;

import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.example.models.dtos.exportDtos.ProcessingJobDto;
import org.example.services.AdminService;
import org.example.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {
    private final AdminService adminService;
    private final UserService userService;

    public AdminRestController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/failed-jobs")
    public ResponseEntity<List<ProcessingJobDto>> getFailedJobs() {
        return ResponseEntity.ok(adminService.getFailedJobs());
    }

    @GetMapping("/monitor-data")
    public ResponseEntity<Map<String, Object>> getFullMonitorData(Principal principal) {
        return ResponseEntity.ok(assembleMonitorData(principal.getName()));
    }

    private Map<String, Object> assembleMonitorData(String email) {
        return Map.of(
                "stats", adminService.getSystemStats(),
                "failedJobs", adminService.getFailedJobs(),
                "currentUser", userService.getUserViewByEmail(email)
        );
    }
}
