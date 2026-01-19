package org.example.web;
import org.example.services.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/monitor")
    public String adminMonitor(Model model) {
        model.addAttribute("jobs", adminService.getFailedJobs());
        model.addAttribute("stats", adminService.getSystemStats());
        return "admin-monitor";
    }


}
