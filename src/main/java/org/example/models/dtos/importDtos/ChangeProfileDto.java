package org.example.models.dtos.importDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.validation.annotation.UniqueEmail;
import org.hibernate.validator.constraints.Length;

public class ChangeProfileDto {
    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    @UniqueEmail(message = "{validation.user.email.unique}")
    private String email;
    @Length(min = 3, max = 20, message = "{validation.user.fullname.length}")
    @NotNull(message = "{validation.user.fullname.required}")
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
