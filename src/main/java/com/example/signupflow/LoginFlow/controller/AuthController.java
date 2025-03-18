package com.example.signupflow.LoginFlow.controller;

import com.example.signupflow.LoginFlow.model.LoginRequest;
import com.example.signupflow.LoginFlow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws Exception {
         authService.sendOtp(request);
         return ResponseEntity.ok().build();
    }
}
