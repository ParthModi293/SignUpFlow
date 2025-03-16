package com.example.signupflow.LoginFlow.controller;

import com.example.signupflow.LoginFlow.model.OtpRequest;
import com.example.signupflow.LoginFlow.model.OtpVerificationRequest;
import com.example.signupflow.LoginFlow.model.ResetPasswordRequest;
import com.example.signupflow.LoginFlow.service.loginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class ForgotPasswordController {

    @Autowired
    private loginService loginService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        return loginService.sendOtp(request.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        return loginService.verifyOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestBody ResetPasswordRequest request) {
        return loginService.resetPassword(email, request);
    }
}
