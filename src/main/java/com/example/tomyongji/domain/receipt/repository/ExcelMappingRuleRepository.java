package com.example.tomyongji.domain.receipt.repository;

import com.example.tomyongji.domain.receipt.entity.ExcelMappingRule;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExcelMappingRuleRepository extends JpaRepository<ExcelMappingRule, Long> {
    Optional<ExcelMappingRule> findByStudentClub(StudentClub studentClub);
}
