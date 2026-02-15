package org.example.controllers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.models.dtos.importDtos.LoginSeedDto;
import org.example.models.dtos.importDtos.RegisterSeedDto;
import org.example.services.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class UserController {
    public static final String ATTR_REGISTER = "registerSeedDto";
    public static final String ATTR_LOGIN = "loginSeedDto";
    private static final String ATTR_REGISTRATION_SUCCESS = "registrationSuccess";
    public static final String ATTR_INVALID_DATA = "invalidData";
    public static final String ATTR_ERROR_MSG = "errorMessage";
    public static final String BINDING_RESULT_PREFIX = "org.springframework.validation.BindingResult.";
    public static final String SPRING_SECURITY_LAST_EXCEPTION = "SPRING_SECURITY_LAST_EXCEPTION";
    private static final String MSG_KEY_INVALID = "error.login.invalid";
    private static final String MSG_KEY_DISABLED = "error.login.disabled";
    private final UserService userService;
    private final MessageSource messageSource;

    public UserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute(ATTR_REGISTER)) {
            model.addAttribute(ATTR_REGISTER, new RegisterSeedDto());
        }
        if(!model.containsAttribute(ATTR_REGISTRATION_SUCCESS)){
            model.addAttribute(ATTR_REGISTRATION_SUCCESS, false);
        }
        return "register";
    }
    @PostMapping("/register")
    public String registerAndSaveInDataBase(@Valid RegisterSeedDto registerSeedDto,
                                            BindingResult bindingResult,
                                            Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        this.userService.register(registerSeedDto);
        model.addAttribute("registrationSuccess", true);
        return "register";
    }

    @GetMapping("/users/verify")
    public String verifyAccount(@RequestParam("token") String token, Model model) {
        boolean isVerified = userService.verifyUser(token);

        if (isVerified) {
            return "verification-success";
        } else {
            model.addAttribute("error", "Invalid or expired verification token.");
            return "login";
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute(ATTR_LOGIN)) {
            model.addAttribute(ATTR_LOGIN, new LoginSeedDto());
        }
        model.addAttribute(ATTR_INVALID_DATA, false);
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model, HttpServletRequest request) {
        model.addAttribute(ATTR_INVALID_DATA, true);
        model.addAttribute(ATTR_ERROR_MSG, getErrorMessage(request));
        model.addAttribute(ATTR_LOGIN, new LoginSeedDto());

        return "login";
    }

    private String getErrorMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Locale locale = LocaleContextHolder.getLocale();

        if (session != null) {
            Object exception = session.getAttribute(SPRING_SECURITY_LAST_EXCEPTION);
            if (exception instanceof DisabledException) {
                return messageSource.getMessage(MSG_KEY_DISABLED, null, locale);
            }
        }
        return messageSource.getMessage(MSG_KEY_INVALID, null, locale);
    }
}
