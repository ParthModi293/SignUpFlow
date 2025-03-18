package com.example.signupflow.LoginFlow.service;

import com.example.signupflow.LoginFlow.entity.LoginDraft;
import com.example.signupflow.LoginFlow.entity.SignInDraft;
import com.example.signupflow.LoginFlow.entity.User;
import com.example.signupflow.LoginFlow.model.LoginRequest;
import com.example.signupflow.LoginFlow.model.LoginSuccessResponse;
import com.example.signupflow.LoginFlow.repositpry.LoginDraftRepository;
import com.example.signupflow.LoginFlow.repositpry.SignInDraftRepository;
import com.example.signupflow.LoginFlow.repositpry.UserRepository;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SignInDraftRepository signInDraftRepository;

    @Autowired
    private LoginDraftRepository loginDraftRepository;


    @Autowired
    private KeycloakAuthService keycloakAuthService;

    private static final int MAX_RESEND_ATTEMPTS = 3;
    private static final int BLOCK_TIME_MINUTES = 10;

    public void sendOtp(LoginRequest request) throws Exception {
        Optional<User> userOptional = userRepository.findByUsername(request.getEmail());

        if(userOptional.isEmpty()) {
            throw new  Exception("User not found");
        }
        User user = userOptional.get();

        LoginDraft loginDraft = loginDraftRepository.findByEmail(request.getEmail()).orElse(null);
        if (loginDraft == null) {
            // ✅ No record found, create a new one
            loginDraft = new LoginDraft();
            loginDraft.setEmail(request.getEmail());
            loginDraft.setUserId(user.getId());
            loginDraft.setResendAttempt(1);
            loginDraft.setOtpGeneratedAt(LocalDateTime.now());
            loginDraft.setOtpExpiredAt(LocalDateTime.now().plusMinutes(10));
        } else {
            // ✅ Step 1: Check if user is blocked
            if (loginDraft.getBlockedUntil() != null && loginDraft.getBlockedUntil().isAfter(LocalDateTime.now())) {
                throw new Exception("Too many OTP requests. Please try again in 10 minutes.");
            }

            // ✅ Step 2: Check if OTP is expired
            if (loginDraft.getOtpExpiredAt().isBefore(LocalDateTime.now())) {
                // Reset OTP fields & attempts
                loginDraft.setOtpGeneratedAt(LocalDateTime.now());
                loginDraft.setOtpExpiredAt(LocalDateTime.now().plusMinutes(10)); // 🔄 Reset to 10 min
                loginDraft.setResendAttempt(1);
            } else {
                // ✅ OTP is still valid, just increment resend attempt
                loginDraft.setResendAttempt(loginDraft.getResendAttempt() + 1);
            }

            // ✅ Step 3: Check resend limit
            if (loginDraft.getResendAttempt() > MAX_RESEND_ATTEMPTS) {
                loginDraft.setBlockedUntil(LocalDateTime.now().plusMinutes(BLOCK_TIME_MINUTES));
                loginDraftRepository.save(loginDraft);
                throw new Exception("Too many OTP requests. Please try again in 10 minutes.");
            }
        }

        // ✅ Generate & save new OTP (Clear old OTP first)
        String otpCode = generateOtp();
        loginDraft.setOtpCode(otpCode);
        loginDraftRepository.save(loginDraft);

        // ✅ Send OTP via email
        sendOtpEmail(request.getEmail(), otpCode);


    }

    private String generateOtp() {
        // Example OTP generation logic (you should enhance it with a more secure method)
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void sendOtpEmail(String email, String otpCode) {
        // Send OTP to the user's email (integrate with your email service)
        System.out.println("Sending OTP " + otpCode + " to email " + email);
    }


}