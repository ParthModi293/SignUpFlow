package com.example.signupflow.LoginFlow.repositpry;

import com.example.signupflow.LoginFlow.entity.LoginDraft;
import com.example.signupflow.LoginFlow.entity.SignInDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignInDraftRepository extends JpaRepository<SignInDraft, Integer> {
    Optional<SignInDraft> findByEmail(String email);
}