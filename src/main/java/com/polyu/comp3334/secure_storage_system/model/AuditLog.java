package com.polyu.comp3334.secure_storage_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String action;
    @Column(nullable = false)
    private String details;
    @Column(nullable = false)
    private String signature;

    // Constructors, getters, setters
    public AuditLog() {}
    public AuditLog(LocalDateTime timestamp, String username, String action, String details, String signature) {
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
        this.details = details;
        this.signature = signature;
    }

    // Getters and setters
    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    @Override
    public String toString() {
        return "AuditLog{" +
                "action='" + action + '\'' +
                ", id=" + id +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                ", details='" + details + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}