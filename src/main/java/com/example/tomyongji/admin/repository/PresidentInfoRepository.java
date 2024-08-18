package com.example.tomyongji.admin.repository;

import com.example.tomyongji.admin.entity.PresidentInfo;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresidentInfoRepository extends JpaRepository<PresidentInfo, Long> {

    PresidentInfo findByStudentNum(String studentNum);

    Boolean existsByStudentNum(String studentNum);
}
