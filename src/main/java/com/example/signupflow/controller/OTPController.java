package com.example.signupflow.controller;

import com.example.signupflow.model.OTPRequest;
import com.example.signupflow.model.OTPVerifyRequest;
import com.example.signupflow.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OTPController {

    @Autowired
    private OTPService otpService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateOTP(@RequestBody OTPRequest request) {
        String otpCode = otpService.generateOTP(request.getEmail(), request.getMobileNumber());
        return ResponseEntity.ok("OTP generated successfully! OTP: " + otpCode);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOTP(@RequestBody OTPVerifyRequest request) {
        try {
            boolean isValid = otpService.verifyOTP(request.getEmail(), request.getOtpCode());
            return ResponseEntity.ok("OTP verified successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
