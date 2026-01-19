package org.example.web.restControllersForTests;

import org.example.models.dtos.exportDtos.AdminStatsDto;
import org.example.models.dtos.exportDtos.ProcessingJobDto;
import org.example.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final AdminService adminService;

    public AdminRestController(AdminService adminService) {
        this.adminService = adminService;
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
    public ResponseEntity<?> getFullMonitorData() {
        return ResponseEntity.ok(Map.of(
                "stats", adminService.getSystemStats(),
                "failedJobs", adminService.getFailedJobs()
        ));
    }
}
