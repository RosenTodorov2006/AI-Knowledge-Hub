package org.example.models.dtos.importDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.validation.annotation.UniqueEmail;
import org.hibernate.validator.constraints.Length;

public class ChangeProfileDto {
    @Email(message = "Invalid email.")
    @NotBlank(message = "Invalid email.")
    @UniqueEmail
    private String email;
    @Length(min = 3, max = 20, message = "Full name length must be between 3 and 20 characters.")
    @NotNull
    private String fullName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
