package org.example.controllers.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class PublicHomeController {
    public static final String VIEW_INDEX = "index";
    public static final String REDIRECT_DASHBOARD = "redirect:/dashboard";

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (isFullyAuthenticated(authentication)) {
            return REDIRECT_DASHBOARD;
        }

        return VIEW_INDEX;
    }
    private boolean isFullyAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }
}
