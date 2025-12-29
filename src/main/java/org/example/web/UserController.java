package org.example.web;

import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.LoginSeedDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {
    @GetMapping("/register")
    public String register(Model model){
        if(!model.containsAttribute("registerSeedDto")){
            model.addAttribute("registerSeedDto",  new RegisterSeedDto());
        }
        // add to model
        return "register";
    }
    @PostMapping("/register")
    public String registerAndSaveInDataBase(@Valid RegisterSeedDto registerSeedDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerSeedDto", bindingResult);
            redirectAttributes.addFlashAttribute("registerSeedDto", registerSeedDto);
            return "redirect:/register";
        }
        // register logic
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
    @GetMapping("/admin")
    public String admin(Model model){
        return "admin";
    }
}
