package org.example.validation.validators;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleRegisterErrors(MethodArgumentNotValidException ex,  HttpServletRequest request,  Model model) {
        if (request.getRequestURI().equals("/register")) {

            List<String> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(f -> f.getDefaultMessage())
                    .collect(Collectors.toList());
            model.addAttribute("errors", errors);
            model.addAttribute("registerSeedDto", ex.getBindingResult().getTarget());

            return "register";
        } else {
            return "/";
        }
    }
}
