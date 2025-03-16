package com.example.signupflow.LoginFlow.repositpry;

import com.example.signupflow.LoginFlow.entity.LoginDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoginDraftRepository extends JpaRepository<LoginDraft, Long> {
  Optional<LoginDraft> findByUsername(String username);
}