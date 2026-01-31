package org.example.models.dtos.importDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.validation.annotation.UniqueEmail;
import org.example.validation.annotation.UniqueUsername;
import org.example.validation.annotation.ValidPasswords;
import org.hibernate.validator.constraints.Length;
@ValidPasswords
public class RegisterSeedDto {
    @Length(min = 3, max = 20, message = "{validation.user.username.length}")
    @NotNull
    @UniqueUsername(message = "{validation.user.username.unique}")
    private String username;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    @UniqueEmail(message = "{validation.user.email.unique}")
    private String email;

    @Length(min = 3, max = 20, message = "{validation.user.password.length}")
    @NotNull
    private String password;

    @Length(min = 3, max = 20, message = "{validation.user.password.confirm.length}")
    @NotNull
    private String confirmPassword;

    @Length(min = 3, max = 20, message = "{validation.user.fullname.length}")
    @NotNull
    private String fullName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
