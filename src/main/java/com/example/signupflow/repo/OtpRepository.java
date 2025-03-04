package com.example.signupflow.repo;

import com.example.signupflow.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {
    Optional<OtpRecord> findByEmail(String email);
}
