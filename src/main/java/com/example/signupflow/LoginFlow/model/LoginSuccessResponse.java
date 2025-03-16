package com.example.signupflow.LoginFlow.model;

import lombok.Data;

@Data
public class LoginSuccessResponse {
    private String grantToken;
    private String refreshToken;
}
