package org.example.models.entities;

import jakarta.persistence.*;
import org.example.models.entities.enums.ApplicationRole;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity{
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false, name = "password")
    private String password;
    @Column(nullable = false, name = "full_name")
    private String username;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationRole applicationRole;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "full_name")
    private String fullName;

    public UserEntity(String email, String password, String username, ApplicationRole applicationRole, boolean active, LocalDateTime createdAt, String fullName) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.applicationRole = applicationRole;
        this.active = active;
        this.createdAt = createdAt;
        this.fullName = fullName;
    }

    public UserEntity() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ApplicationRole getRole() {
        return applicationRole;
    }

    public void setRole(ApplicationRole applicationRole) {
        this.applicationRole = applicationRole;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
