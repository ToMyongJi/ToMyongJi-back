package com.example.tomyongji.domain.auth.repository;

import com.example.tomyongji.domain.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    void deleteByEmail(String email);
    Optional<EmailVerification> findByEmail(String email);
    List<EmailVerification> findByEmailOrderByVerificatedAtDesc(String email);

}
