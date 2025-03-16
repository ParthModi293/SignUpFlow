package com.example.signupflow.LoginFlow.service;

import com.example.signupflow.LoginFlow.entity.LoginDraft;
import com.example.signupflow.LoginFlow.entity.User;
import com.example.signupflow.LoginFlow.model.LoginRequest;
import com.example.signupflow.LoginFlow.model.LoginSuccessResponse;
import com.example.signupflow.LoginFlow.repositpry.LoginDraftRepository;
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
    private LoginDraftRepository loginDraftRepository;



    @Autowired
    private KeycloakAuthService keycloakAuthService;

    public ResponseEntity<?> login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        User user = userOptional.get();

        // Fetch or create LoginDraft entry
        LoginDraft loginDraft = loginDraftRepository.findByUsername(request.getUsername())
                .orElse(new LoginDraft(request.getUsername()));

        // Check if user is blocked
        if (loginDraft.getBlockedUntil() != null && loginDraft.getBlockedUntil().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked. Try again later.");
        }

        // Validate Password in User Table
        if (!request.getPassword().equals(user.getPassword())) {
            return handleFailedAttempt(loginDraft);
        }

        // Authenticate with Keycloak
        if (!keycloakAuthService.authenticateUserFromKeycloak(request.getUsername(), request.getPassword())) {
            return handleFailedAttempt(loginDraft);
        }

        // Reset failed attempts on success
        loginDraft.setFailedAttempts(0);
        loginDraft.setLastFailedAttempt(null);
        loginDraft.setBlockedUntil(null);
        loginDraftRepository.save(loginDraft);

        // Get token from Keycloak
        AccessTokenResponse tokenResponse = keycloakAuthService.getTokensFromKeycloak(request.getUsername(), request.getPassword());

        LoginSuccessResponse loginSuccessResponse = new LoginSuccessResponse();
        loginSuccessResponse.setGrantToken(tokenResponse.getToken());
        loginSuccessResponse.setRefreshToken(tokenResponse.getRefreshToken());

        return ResponseEntity.ok(loginSuccessResponse);
    }

    private ResponseEntity<String> handleFailedAttempt(LoginDraft loginDraft) {
        LocalDateTime now = LocalDateTime.now();

        // Reset failed attempts if last attempt was more than 10 minutes ago
        if (loginDraft.getLastFailedAttempt() != null && loginDraft.getLastFailedAttempt().isBefore(now.minusMinutes(10))) {
            loginDraft.setFailedAttempts(1); // Reset and start fresh count
        } else {
            loginDraft.setFailedAttempts(loginDraft.getFailedAttempts() + 1);
        }

        loginDraft.setLastFailedAttempt(now); // Update last failed attempt time

        // Lock account if failed attempts exceed limit
        if (loginDraft.getFailedAttempts() >= 5) {
            loginDraft.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
            loginDraft.setFailedAttempts(0); // Reset count after lock
        }

        loginDraftRepository.save(loginDraft);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }
}
