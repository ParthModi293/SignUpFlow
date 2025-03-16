package com.example.signupflow.LoginFlow.repositpry;

import com.example.signupflow.LoginFlow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
