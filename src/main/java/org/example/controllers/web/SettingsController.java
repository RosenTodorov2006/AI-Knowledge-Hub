package org.example.controllers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.example.exceptions.InvalidPasswordException;
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
import java.util.Locale;

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
    private final MessageSource messageSource;
    public SettingsController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
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
    public String changeInfo(@Valid ChangeProfileDto changeProfileDto, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes, Principal principal, Locale locale) {
        if (bindingResult.hasErrors()) {
            return handleBindingError(redirectAttributes, ATTR_CHANGE_PROFILE, changeProfileDto, bindingResult, ATTR_INVALID_PROFILE);
        }

        try {
            userService.changeProfileInfo(changeProfileDto, principal.getName());
            if (!principal.getName().equals(changeProfileDto.getEmail())) updateSecurityContext(changeProfileDto.getEmail());
            return "redirect:/settings?success=true";
        } catch (InvalidPasswordException e) {
            return handleSecurityError(redirectAttributes, e, locale, ATTR_CHANGE_PROFILE, changeProfileDto, ATTR_INVALID_PROFILE, "profileError");
        }
    }

    @PostMapping("/changeUserPassword")
    public String changeUserPassword(@Valid ChangeUserPasswordDto changeUserPasswordDto, BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes, Principal principal, Locale locale) {
        if (bindingResult.hasErrors()) {
            return handleBindingError(redirectAttributes, ATTR_CHANGE_PASSWORD, changeUserPasswordDto, bindingResult, ATTR_INVALID_PASSWORD);
        }

        try {
            userService.changeUserPassword(changeUserPasswordDto, principal.getName());
            updateSecurityContext(principal.getName());
            return "redirect:/settings?pwSuccess=true";
        } catch (InvalidPasswordException e) {
            return handleSecurityError(redirectAttributes, e, locale, ATTR_CHANGE_PASSWORD, changeUserPasswordDto, ATTR_INVALID_PASSWORD, "passwordError");
        }
    }

    @PostMapping("/deactivate")
    public String disableAccount(@ModelAttribute("userDeactivateDto") UserDeactivateDto deactivateDto,
                                 Principal principal,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        try {
            userService.disableUser(principal.getName(), deactivateDto.getCurrentPassword());
            performLogout(request, response);
            return "redirect:/?deactivated=true";
        } catch (InvalidPasswordException e) {
            return handleSecurityError(redirectAttributes, e, locale, null, null, null, "deactivateError");
        }
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

    private String handleBindingError(RedirectAttributes ra, String attr, Object dto, BindingResult br, String flag) {
        handleBindingErrors(ra, attr, dto, br, flag);
        return "redirect:/settings";
    }

    private String handleSecurityError(RedirectAttributes ra, InvalidPasswordException e, Locale l,
                                       String attr, Object dto, String flag, String errorKey) {
        if (attr != null) ra.addFlashAttribute(attr, dto);
        if (flag != null) ra.addFlashAttribute(flag, true);

        String translatedError = messageSource.getMessage(e.getMessage(), null, l);
        ra.addFlashAttribute(errorKey, translatedError);
        return "redirect:/settings";
    }

    private void performLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
    }
}