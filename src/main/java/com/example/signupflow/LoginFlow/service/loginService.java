package com.example.signupflow.LoginFlow.service;

import com.example.signupflow.LoginFlow.entity.LoginDraft;
import com.example.signupflow.LoginFlow.entity.User;
import com.example.signupflow.LoginFlow.model.ResetPasswordRequest;
import com.example.signupflow.LoginFlow.repositpry.LoginDraftRepository;
import com.example.signupflow.LoginFlow.repositpry.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class loginService {

    @Autowired
    private LoginDraftRepository loginDraftRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> sendOtp(String email) {
        Optional<User> userOptional = userRepository.findByUsername(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOptional.get();
        LoginDraft loginDraft = loginDraftRepository.findByUsername(user.getUsername())
                .orElse(new LoginDraft(user.getUsername()));

        // Check if user is blocked due to too many resends
        if (loginDraft.getOtpBlockedUntil() != null && loginDraft.getOtpBlockedUntil().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Too many OTP requests. Try again later.");
        }

        // Allow only 3 OTP resend requests within 10 minutes
        if (loginDraft.getResendCount() >= 3) {
            loginDraft.setOtpBlockedUntil(LocalDateTime.now().plusMinutes(10));
            loginDraft.setResendCount(0);
            loginDraftRepository.save(loginDraft);
            /*SendMailForBlock*/
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Too many OTP resends. Try again later.");
        }

        // Generate OTP and store its creation time
        String otp = "134625";
        loginDraft.setOtpCode(otp);
        loginDraft.setOtpCreatedAt(LocalDateTime.now()); // Track when OTP was created
        loginDraft.setOtpAttempts(0);  // Reset OTP attempts
        loginDraft.setResendCount(loginDraft.getResendCount() + 1);
        loginDraftRepository.save(loginDraft);

        /*sendOtpEmail(email, otp);*/
        return ResponseEntity.ok("OTP sent successfully");
    }





    public ResponseEntity<?> verifyOtp(String email, String otp) {
        Optional<User> userOptional = userRepository.findByUsername(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOptional.get();
       Optional<LoginDraft> loginDraft1 = loginDraftRepository.findByUsername(user.getUsername());
       if(loginDraft1.isEmpty()) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
       }
        LoginDraft loginDraft = loginDraft1.get();


        // Check if OTP has expired (older than 10 minutes)
        if (loginDraft.getOtpCreatedAt() != null &&
                loginDraft.getOtpCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            loginDraft.setOtpCode(null); // Clear expired OTP
            loginDraft.setOtpCreatedAt(null);
            loginDraftRepository.save(loginDraft);
            return ResponseEntity.status(HttpStatus.GONE).body("OTP expired. Please request a new one.");
        }

        // Check if user is blocked due to too many OTP failures
        if (loginDraft.getOtpBlockedUntil() != null && loginDraft.getOtpBlockedUntil().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Too many failed attempts. Try again later.");
        }

        // Validate OTP
        if (!otp.equals(loginDraft.getOtpCode())) {
            loginDraft.setOtpAttempts(loginDraft.getOtpAttempts() + 1);

            // Block user after 3 failed OTP attempts
            if (loginDraft.getOtpAttempts() >= 3) {
                loginDraft.setOtpBlockedUntil(LocalDateTime.now().plusMinutes(10));
                loginDraft.setOtpAttempts(0); // Reset count after block
            }

            loginDraftRepository.save(loginDraft);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }

        // Reset OTP fields on success
        loginDraft.setOtpCode(null);
        loginDraft.setOtpCreatedAt(null);
        loginDraft.setOtpAttempts(0);
        loginDraft.setOtpBlockedUntil(null);
        loginDraft.setResendCount(0);
        loginDraftRepository.save(loginDraft);

        return ResponseEntity.ok("OTP verified successfully");
    }







    public ResponseEntity<?> resetPassword(String email, ResetPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOptional.get();
        LoginDraft loginDraft = loginDraftRepository.findByUsername(user.getUsername())
                .orElse(new LoginDraft(user.getUsername()));

        // Ensure OTP was verified before allowing password reset
        if (loginDraft.getOtpCode() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("OTP verification required before resetting password.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }

}
