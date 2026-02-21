package org.example.controllers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.models.dtos.exportDtos.UserViewDto;
import org.example.models.dtos.importDtos.ChangeProfileDto;
import org.example.models.dtos.importDtos.ChangeUserPasswordDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.models.dtos.importDtos.UserDeactivateDto;
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

        if (!model.containsAttribute(ATTR_CHANGE_PASSWORD)) {
            model.addAttribute(ATTR_CHANGE_PASSWORD, new ChangeUserPasswordDto());
        }

        if (!model.containsAttribute("userDeactivateDto")) {
            model.addAttribute("userDeactivateDto", new UserDeactivateDto());
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
        boolean success = userService.changeProfileInfo(changeProfileDto, principal.getName());
        if (!success) {
            redirectAttributes.addFlashAttribute(ATTR_CHANGE_PROFILE, changeProfileDto);
            redirectAttributes.addFlashAttribute(ATTR_INVALID_PROFILE, true);
            redirectAttributes.addFlashAttribute("profileError", "Invalid password.");
            return "redirect:/settings";
        }
        String currentEmail = principal.getName();
        if (!currentEmail.equals(changeProfileDto.getEmail())) {
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

        boolean success = userService.changeUserPassword(changeUserPasswordDto, principal.getName());

        if (!success) {
            redirectAttributes.addFlashAttribute(ATTR_CHANGE_PASSWORD, changeUserPasswordDto);
            redirectAttributes.addFlashAttribute(ATTR_INVALID_PASSWORD, true);
            redirectAttributes.addFlashAttribute("passwordError", "Current password does not match.");
            return "redirect:/settings";
        }

        updateSecurityContext(principal.getName());
        return "redirect:/settings?pwSuccess=true";
    }

    @DeleteMapping()
    public String disableAccount(@ModelAttribute("userDeactivateDto") UserDeactivateDto deactivateDto,
                                 Principal principal,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) {

        boolean success = userService.deleteUser(principal.getName(), deactivateDto.getCurrentPassword());

        if (!success) {
            redirectAttributes.addFlashAttribute("deactivateError", "Invalid password. Account not deactivated.");
            return "redirect:/settings";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/?deactivated=true";
    }
    @PostMapping("/toggle-emails")
    public String toggleEmails(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleEmailNotifications(principal.getName());
            redirectAttributes.addFlashAttribute("success", "Email preferences updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not update email preferences.");
        }
        return "redirect:/settings";
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