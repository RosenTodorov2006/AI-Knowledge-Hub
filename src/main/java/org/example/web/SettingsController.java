package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.models.entities.UserEntity;
import org.example.services.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
    public String settings(Model model, Principal principal){
        if (!model.containsAttribute("changeProfileDto")) {
            UserViewDto userViewByEmail = userService.getUserViewByEmail(principal.getName());
            ChangeProfileDto dto = new ChangeProfileDto();
            dto.setEmail(userViewByEmail.getEmail());
            dto.setFullName(userViewByEmail.getFullName());
            model.addAttribute("changeProfileDto", dto);
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
    public String changeInfo(@Valid ChangeProfileDto changeProfileDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changeProfileDto", bindingResult);
            redirectAttributes.addFlashAttribute("changeProfileDto", changeProfileDto);
            redirectAttributes.addFlashAttribute("invalidProfileInfoData", true);

            return "redirect:/settings";
        }

        String oldEmail = principal.getName();
        userService.changeProfileInfo(changeProfileDto, oldEmail);

        if (!oldEmail.equals(changeProfileDto.getEmail())) {
            updateSecurityContext(changeProfileDto.getEmail());
        }

        return "redirect:/settings?success=true";
    }

    private void updateSecurityContext(String newEmail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newEmail,
                auth.getCredentials(),
                auth.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
    @PostMapping("/changeUserPassword")
    public String changeUserPassword(@Valid ChangeUserPasswordDto changeUserPasswordDto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changeUserPasswordDto", bindingResult);
            redirectAttributes.addFlashAttribute("changeUserPasswordDto", changeUserPasswordDto);
            redirectAttributes.addFlashAttribute("invalidUserPasswordData", true);
            return "redirect:/settings";
        }
        userService.changeUserPassword(changeUserPasswordDto, principal.getName());

        updateSecurityContext(principal.getName());

        return "redirect:/settings?pwSuccess=true";
    }
    @DeleteMapping()
    public String disableAccount(Principal principal, HttpServletRequest request, HttpServletResponse response) {
        userService.deleteUser(principal.getName());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/?deactivated=true";
    }
}
