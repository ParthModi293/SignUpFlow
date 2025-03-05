package com.example.signupflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRecord {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 4)
    private String otpCode;



    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiredAt;

    private int retryAttempt ;
    private int resendAttempt ;
    private Instant blockTime ;
}
