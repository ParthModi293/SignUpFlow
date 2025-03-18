package com.example.signupflow.LoginFlow.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String password;
    
    private int failedAttempts = 0;
    private LocalDateTime lockedUntil;

    // Getters and Setters
}
