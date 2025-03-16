package com.example.signupflow.LoginFlow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_draft")
@Data
public class LoginDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Integer failedAttempts = 0;
    private LocalDateTime blockedUntil;

    private String otpCode;
    private Integer otpAttempts = 0;
    private Integer resendCount = 0;
    private LocalDateTime otpBlockedUntil;
    private LocalDateTime otpCreatedAt;
    private  LocalDateTime lastFailedAttempt;



    private LocalDateTime otpExpiredAt;


    public LoginDraft() {
    }

    public LoginDraft(String username) {
        this.username = username;
    }
}
