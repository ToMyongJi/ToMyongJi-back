package com.example.tomyongji.admin.repository;

import com.example.tomyongji.admin.entity.President;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresidentRepository extends JpaRepository<President, Long> {

    President findByStudentNum(String studentNum);

    Boolean existsByStudentNum(String studentNum);
}
