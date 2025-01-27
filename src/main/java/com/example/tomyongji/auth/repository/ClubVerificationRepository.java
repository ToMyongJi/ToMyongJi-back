package com.example.tomyongji.auth.repository;

import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubVerificationRepository extends JpaRepository<ClubVerification, Long> {
    List<ClubVerification> findByStudentNum(String studentNum);

    void deleteByStudentNum(String studentNum);
}
