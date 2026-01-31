package org.example.controllers.web;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@ControllerAdvice
public class GlobalExceptionHandler {
    public static final String ATTR_STATUS = "status";
    public static final String ATTR_ERROR = "error";
    public static final String ATTR_MESSAGE = "message";
    private static final String MSG_KEY_INTERNAL_ERROR = "error.internal.server";
    private static final String MSG_KEY_UNEXPECTED = "error.unexpected";

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model) {
        Locale locale = LocaleContextHolder.getLocale();

        model.addAttribute(ATTR_STATUS, ex.getStatusCode().value());
        model.addAttribute(ATTR_ERROR, ex.getStatusCode().toString());
        String translatedMessage = messageSource.getMessage(ex.getReason(), null, ex.getReason(), locale);
        model.addAttribute(ATTR_MESSAGE, translatedMessage);

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        Locale locale = LocaleContextHolder.getLocale();

        String errorTitle = messageSource.getMessage(MSG_KEY_INTERNAL_ERROR, null, locale);
        String errorMessage = messageSource.getMessage(MSG_KEY_UNEXPECTED, null, locale);

        model.addAttribute(ATTR_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute(ATTR_ERROR, errorTitle);
        model.addAttribute(ATTR_MESSAGE, errorMessage);
        ex.printStackTrace();

        return "error";
    }
}