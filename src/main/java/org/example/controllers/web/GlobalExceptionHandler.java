package org.example.controllers.web;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {
    public static final String VIEW_ERROR = "error";
    public static final String ATTR_STATUS = "status";
    public static final String ATTR_ERROR = "error";
    public static final String ATTR_MESSAGE = "message";
    public static final String MSG_INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String MSG_UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model) {
        model.addAttribute(ATTR_STATUS, ex.getStatusCode().value());
        model.addAttribute(ATTR_ERROR, ex.getStatusCode().toString());
        model.addAttribute(ATTR_MESSAGE, ex.getReason());
        return VIEW_ERROR;
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute(ATTR_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute(ATTR_ERROR, MSG_INTERNAL_SERVER_ERROR);
        model.addAttribute(ATTR_MESSAGE, MSG_UNEXPECTED_ERROR);
        ex.printStackTrace();
        return VIEW_ERROR;
    }
}