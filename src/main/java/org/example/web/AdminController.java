package org.example.web;
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
    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/monitor")
    public String adminMonitor(Model model, Principal principal) {
        model.addAttribute("jobs", adminService.getFailedJobs());
        model.addAttribute("stats", adminService.getSystemStats());
        UserViewDto currentUser = this.userService.getUserViewByEmail(principal.getName());
        model.addAttribute("currentUser", currentUser);
        return "admin-monitor";
    }


}
