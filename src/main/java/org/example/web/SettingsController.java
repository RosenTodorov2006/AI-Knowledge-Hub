package org.example.web;

import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/settings")
public class SettingsController {
    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String settings(Model model){
        if (!model.containsAttribute("changeProfileDto")) {
            model.addAttribute("changeProfileDto", new ChangeProfileDto());
        }
        if (!model.containsAttribute("changeUserPasswordDto")) {
            model.addAttribute("changeUserPasswordDto", new ChangeUserPasswordDto());
        }
        if (!model.containsAttribute("invalidProfileInfoData")) {
            model.addAttribute("invalidProfileInfoData", false);
        }
        if (!model.containsAttribute("invalidUserPasswordData")) {
            model.addAttribute("invalidUserPasswordData", false);
        }
        return "settings";
    }
    @PostMapping("/changeInfo")
    public String changeInfo(@Valid ChangeProfileDto changeProfileDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, Principal principal){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changeProfileDto", bindingResult);
            redirectAttributes.addFlashAttribute("changeProfileDto", changeProfileDto);
            redirectAttributes.addFlashAttribute("invalidProfileInfoData", true);
            return "redirect:/settings";
        }
        userService.changeProfileInfo(changeProfileDto, principal.getName());
        // фиелд
        return "redirect:/settings";
    }
    @PostMapping("/changeUserPassword")
    public String changeUserPassword(@Valid ChangeUserPasswordDto changeUserPasswordDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, Principal principal){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changeUserPasswordDto", bindingResult);
            redirectAttributes.addFlashAttribute("changeUserPasswordDto", changeUserPasswordDto);
            redirectAttributes.addFlashAttribute("invalidUserPasswordData", true);
            return "redirect:/settings";
        }
        userService.changeUserPassword(changeUserPasswordDto, principal.getName());
        return "redirect:/settings";
    }
    @DeleteMapping()
    public String delete(Principal principal){
        userService.deleteUser(principal.getName());
        return "redirect:/";
    }
}
