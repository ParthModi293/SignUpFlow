package com.example.signupflow.LoginFlow.model;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String newPassword;
    private String confirmPassword;
}
