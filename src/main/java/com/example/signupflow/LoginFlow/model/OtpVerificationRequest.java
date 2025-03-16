package com.example.signupflow.LoginFlow.model;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email;
    private String otp;
}
