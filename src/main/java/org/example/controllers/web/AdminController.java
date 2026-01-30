package org.example.controllers.web;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.services.AdminService;
import org.example.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {
    public static final String ATTR_JOBS = "jobs";
    public static final String ATTR_STATS = "stats";
    public static final String ATTR_CURRENT_USER = "currentUser";
    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/monitor")
    public String adminMonitor(Model model, Principal principal) {
        populateMonitorModel(model, principal.getName());

        return "admin-monitor";
    }

    private void populateMonitorModel(Model model, String email) {
        model.addAttribute(ATTR_JOBS, adminService.getFailedJobs());
        model.addAttribute(ATTR_STATS, adminService.getSystemStats());
        model.addAttribute(ATTR_CURRENT_USER, userService.getUserViewByEmail(email));
    }
}
