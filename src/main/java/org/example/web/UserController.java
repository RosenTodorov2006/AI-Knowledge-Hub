package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.LoginSeedDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.services.UserService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
    private final UserService  userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String register(Model model){
        if(!model.containsAttribute("registerSeedDto")){
            model.addAttribute("registerSeedDto",  new RegisterSeedDto());
        }
        return "register";
    }
    @PostMapping("/register")
    public String registerAndSaveInDataBase(@Valid RegisterSeedDto registerSeedDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerSeedDto", bindingResult);
            redirectAttributes.addFlashAttribute("registerSeedDto", registerSeedDto);
            return "redirect:/register";
        }
        this.userService.register(registerSeedDto);
        return "redirect:/login";
    }
    @GetMapping("/login")
    public String login(Model model){
        if(!model.containsAttribute("loginSeedDto")){
            model.addAttribute("loginSeedDto", new LoginSeedDto());
        }
        model.addAttribute("invalidData", false);
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String errorMessage = "Invalid username or password.";

        if (session != null) {
            Exception ex = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            if (ex instanceof DisabledException) {
                errorMessage = "Your account is deactivated. Please contact support.";
            }
        }

        model.addAttribute("invalidData", true);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("loginSeedDto", new LoginSeedDto());
        return "login";
    }
    @GetMapping("/admin")
    public String admin(Model model){
        return "admin";
    }
}
