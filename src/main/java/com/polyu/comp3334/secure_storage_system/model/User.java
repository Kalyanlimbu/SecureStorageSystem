package com.polyu.comp3334.secure_storage_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    // Security audit fields
    @Column(updatable = false)
    private LocalDateTime registerAt = LocalDateTime.now();

    //@Column(updatable = false)
    @Column
    private LocalDateTime lastLogin;

    //@Column(updatable = false)
    @Column
    private LocalDateTime lastLogout;

    @Column
    private boolean isAdmin = false;

    // Default constructor (required by JPA)
    public User() {
    }

    // Combined constructor (id is optional)
    public User(String username, String password, String email, LocalDateTime registerAt, Boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.registerAt = registerAt;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() { return isAdmin; }

    public void setAdmin(boolean admin) { isAdmin = admin; }

    public LocalDateTime getLastLogin() { return lastLogin; }

    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getLastLogout() { return lastLogout; }

    public void setLastLogout(LocalDateTime lastLogout) { this.lastLogout = lastLogout; }

    public LocalDateTime getRegisterAt() { return registerAt; }

    public void setRegisterAt(LocalDateTime registerAt) { this.registerAt = registerAt; }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", registerAt=" + registerAt +
                ", lastLogin=" + lastLogin +
                ", lastLogout=" + lastLogout +
                ", isAdmin=" + isAdmin +
                '}';
    }
}