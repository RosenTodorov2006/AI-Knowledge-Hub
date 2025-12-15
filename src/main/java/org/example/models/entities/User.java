package org.example.models.entities;

import jakarta.persistence.*;
import org.example.models.entities.enums.ApplicationRole;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends BaseEntity{
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;
    @Column(nullable = false, name = "full_name")
    private String fullName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationRole applicationRole;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(String email, String passwordHash, String fullName, ApplicationRole applicationRole, boolean active, LocalDateTime createdAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.applicationRole = applicationRole;
        this.active = active;
        this.createdAt = createdAt;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
}
