package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllByStudentClub(StudentClub studentClub);
    boolean existsByDateAndContent(Date date, String content);
    void deleteAllByStudentClub(StudentClub studentClub);
    int countByStudentClub(StudentClub studentClub);
    int countByStudentClubAndVerificationTrue(StudentClub studentClub);


    List<Receipt> findByStudentClubAndDateBetween(StudentClub studentClub, Date startDate, Date endDate);
}
