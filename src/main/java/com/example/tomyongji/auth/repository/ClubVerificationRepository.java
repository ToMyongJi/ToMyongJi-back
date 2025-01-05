package com.example.tomyongji.auth.repository;

import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubVerificationRepository extends JpaRepository<ClubVerification, Long> {
    Optional<ClubVerification> findByStudentNum(String studentNum);
}
