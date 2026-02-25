package org.example.exceptions;
public class InvalidPasswordException extends RuntimeException {
    public static final String I18N_KEY = "settings.error.password_mismatch";

    public InvalidPasswordException() {
        super(I18N_KEY);
    }
}