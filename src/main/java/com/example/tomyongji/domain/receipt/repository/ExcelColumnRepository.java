package com.example.tomyongji.domain.receipt.repository;
import com.example.tomyongji.domain.receipt.entity.ExcelColumn;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExcelColumnRepository extends JpaRepository<ExcelColumn, Long> {

    Optional<ExcelColumn> findByStudentClub(StudentClub studentClub);

}