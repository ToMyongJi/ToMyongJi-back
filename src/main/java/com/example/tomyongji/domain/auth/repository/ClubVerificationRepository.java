package com.example.tomyongji.domain.auth.repository;

import com.example.tomyongji.domain.auth.entity.ClubVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubVerificationRepository extends JpaRepository<ClubVerification, Long> {
    List<ClubVerification> findByStudentNum(String studentNum);

    void deleteByStudentNum(String studentNum);
}
