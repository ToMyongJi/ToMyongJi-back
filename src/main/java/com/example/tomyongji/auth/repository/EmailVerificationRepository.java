package com.example.tomyongji.auth.repository;

import com.example.tomyongji.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    void deleteByEmail(String email);
    Optional<EmailVerification> findByEmail(String email);
}
