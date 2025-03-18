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

    private Long userId;
    private String email;
    private String otpCode;
    private LocalDateTime otpGeneratedAt;
    private LocalDateTime otpExpiredAt;
    private int resendAttempt;
    private LocalDateTime blockedUntil;


    public LoginDraft() {
    }


}
