package com.polyu.comp3334.secure_storage_system.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "pwdresettokens")
public class PwdResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** 6 digit OTP pin **/
    @Column(nullable = false)
    private String token;

    /** join column. I think we still using username as id. check **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="username", nullable=false)
    private User user;

    /** expiry of token **/
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /** marking used token column **/
    @Column(nullable = false)
    private boolean used = false;

    // need to add getters, and setters later (after talk with K and Angad)
    public PwdResetToken(){}
    public PwdResetToken(User user, String token, LocalDateTime expiryDate){
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
        this.used = false;
    }

    public void setUsed(boolean b) {
        this.used = true;
    }

    public boolean isUsed() {
        return this.used;
    }

    public Instant getExpiryDate() {
        return this.expiryDate.toInstant(ZoneOffset.UTC);
    }
    public String getToken() { return token; }

    public User getUser() {
        return this.user;
    }
}
