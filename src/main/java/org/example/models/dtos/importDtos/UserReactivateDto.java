package org.example.models.dtos.importDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.validation.annotation.UniqueEmail;

public class UserReactivateDto {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    public UserReactivateDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserReactivateDto() {
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
}
