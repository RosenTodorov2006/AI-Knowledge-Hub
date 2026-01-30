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
    @GetMapping("/")
    public String index(Authentication authentication) {
        if (isFullyAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }

        return "index";
    }
    private boolean isFullyAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }
}
