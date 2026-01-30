package org.example.controllers.web;

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
    public static final String ATTR_CHANGE_PROFILE = "changeProfileDto";
    public static final String ATTR_CHANGE_PASSWORD = "changeUserPasswordDto";
    public static final String ATTR_INVALID_PROFILE = "invalidProfileInfoData";
    public static final String ATTR_INVALID_PASSWORD = "invalidUserPasswordData";
    public static final String ATTR_CURRENT_USER = "currentUser";
    public static final String BINDING_RESULT_PREFIX = "org.springframework.validation.BindingResult.";

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String settings(Model model, Principal principal) {
        String email = principal.getName();

        if (!model.containsAttribute(ATTR_CHANGE_PROFILE)) {
            model.addAttribute(ATTR_CHANGE_PROFILE, userService.getChangeProfileDto(email));
        }

        ensureDefaultAttributes(model);
        model.addAttribute(ATTR_CURRENT_USER, userService.getUserViewByEmail(email));

        return "settings";
    }

    @PostMapping("/changeInfo")
    public String changeInfo(@Valid ChangeProfileDto changeProfileDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {

        if (bindingResult.hasErrors()) {
            handleBindingErrors(redirectAttributes, ATTR_CHANGE_PROFILE, changeProfileDto, bindingResult, ATTR_INVALID_PROFILE);
            return "redirect:/settings";
        }

        String oldEmail = principal.getName();
        userService.changeProfileInfo(changeProfileDto, oldEmail);

        if (!oldEmail.equals(changeProfileDto.getEmail())) {
            updateSecurityContext(changeProfileDto.getEmail());
        }

        return "redirect:/settings?success=true";
    }

    @PostMapping("/changeUserPassword")
    public String changeUserPassword(@Valid ChangeUserPasswordDto changeUserPasswordDto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Principal principal) {
        if (bindingResult.hasErrors()) {
            handleBindingErrors(redirectAttributes, ATTR_CHANGE_PASSWORD, changeUserPasswordDto, bindingResult, ATTR_INVALID_PASSWORD);
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

    private void ensureDefaultAttributes(Model model) {
        if (!model.containsAttribute(ATTR_CHANGE_PASSWORD)) {
            model.addAttribute(ATTR_CHANGE_PASSWORD, new ChangeUserPasswordDto());
        }
        if (!model.containsAttribute(ATTR_INVALID_PROFILE)) {
            model.addAttribute(ATTR_INVALID_PROFILE, false);
        }
        if (!model.containsAttribute(ATTR_INVALID_PASSWORD)) {
            model.addAttribute(ATTR_INVALID_PASSWORD, false);
        }
    }

    private void handleBindingErrors(RedirectAttributes ra, String attrName, Object dto, BindingResult br, String errorFlag) {
        ra.addFlashAttribute(BINDING_RESULT_PREFIX + attrName, br);
        ra.addFlashAttribute(attrName, dto);
        ra.addFlashAttribute(errorFlag, true);
    }

    private void updateSecurityContext(String newEmail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(newEmail, auth.getCredentials(), auth.getAuthorities())
        );
    }
}