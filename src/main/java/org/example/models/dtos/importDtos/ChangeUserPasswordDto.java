package org.example.models.dtos.importDtos;

import jakarta.validation.constraints.NotNull;
import org.example.validation.annotation.ValidPasswords;
import org.hibernate.validator.constraints.Length;
@ValidPasswords(message = "{validation.user.passwords.match}")
public class ChangeUserPasswordDto {

    @Length(min = 3, max = 20, message = "{validation.user.password.length}")
    @NotNull
    private String password;

    @Length(min = 3, max = 20, message = "{validation.user.password.confirm.length}")
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
