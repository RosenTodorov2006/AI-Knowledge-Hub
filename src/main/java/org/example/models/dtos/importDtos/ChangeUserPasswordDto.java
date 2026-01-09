package org.example.models.dtos.importDtos;

import jakarta.validation.constraints.NotNull;
import org.example.validation.annotation.ValidPasswords;
import org.hibernate.validator.constraints.Length;
@ValidPasswords
public class ChangeUserPasswordDto {
    @Length(min = 3, max = 20, message = "Password length must be between 3 and 20 characters.")
    @NotNull
    private String password;
    @Length(min = 3, max = 20, message = "Confirm password length must be between 3 and 20 characters.")
    @NotNull
    private String confirmPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
