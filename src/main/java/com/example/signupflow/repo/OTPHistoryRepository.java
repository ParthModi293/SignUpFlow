package com.example.signupflow.repo;

import com.example.signupflow.entity.OtpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPHistoryRepository extends JpaRepository<OtpHistory, Long> {
    Optional<OtpHistory> findByEmail(String email);
}
