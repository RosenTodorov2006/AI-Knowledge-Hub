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
    @Length(min = 3, max = 20, message = "Name length must be between 3 and 20 characters.")
    @NotNull
    @UniqueUsername
    private String username;
    @Email(message = "Invalid email.")
    @NotBlank(message = "Invalid email.")
    @UniqueEmail
    private String email;
    @Length(min = 3, max = 20, message = "Password length must be between 3 and 20 characters.")
    @NotNull
    private String password;
    @Length(min = 3, max = 20, message = "Confirm password length must be between 3 and 20 characters.")
    @NotNull
    private String confirmPassword;
    @Length(min = 3, max = 20, message = "Full name length must be between 3 and 20 characters.")
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
