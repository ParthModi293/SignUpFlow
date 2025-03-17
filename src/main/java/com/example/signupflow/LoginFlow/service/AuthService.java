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

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SignInDraftRepository signInDraftRepository;


    @Autowired
    private KeycloakAuthService keycloakAuthService;

    public ResponseEntity<?> login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        User user = userOptional.get();

        // Fetch or create LoginDraft entry
        SignInDraft loginAttempt = signInDraftRepository.findByEmail(user.getUsername())
                .orElseGet(() -> {
                    SignInDraft newAttempt = new SignInDraft();
                    newAttempt.setEmail(request.getUsername());
                    return newAttempt;
                });

        // Check if the first failed attempt was more than 10 minutes ago
        if (loginAttempt.getFirstFailedTime() != null &&
                loginAttempt.getFirstFailedTime().plusMinutes(10).isBefore(LocalDateTime.now())) {

            // Reset all fields
            loginAttempt.setFailedAttempts(0);
            loginAttempt.setFirstFailedTime(null);
            loginAttempt.setBlockedUntil(null);
        }

        // Check if user is blocked
        if (loginAttempt.getBlockedUntil()!=null && loginAttempt.getBlockedUntil().isAfter(LocalDateTime.now())) {
            return  ResponseEntity.ok("User blocked"); // User is still blocked
        }

        boolean isValidLogin = validateLogin(request);
        if (isValidLogin) {
            loginAttempt.setFailedAttempts(0);
            loginAttempt.setFirstFailedTime(null);
            loginAttempt.setBlockedUntil(null);
            signInDraftRepository.save(loginAttempt);
            return ResponseEntity.ok("Login successful");
        }else{
            if (loginAttempt.getFailedAttempts() == 0) {
                loginAttempt.setFirstFailedTime(LocalDateTime.now());
            }
            loginAttempt.setFailedAttempts(loginAttempt.getFailedAttempts() + 1);

            if (loginAttempt.getFailedAttempts() >= 5) {
                loginAttempt.setBlockedUntil(loginAttempt.getFirstFailedTime().plusMinutes(10));
            }
            signInDraftRepository.save(loginAttempt);
            return ResponseEntity.ok("Login Failure");
        }


    }

    private boolean validateLogin(LoginRequest request) {
        return request.getUsername().equals(request.getUsername()) ; // Replace with hashed password check in production
    }
}