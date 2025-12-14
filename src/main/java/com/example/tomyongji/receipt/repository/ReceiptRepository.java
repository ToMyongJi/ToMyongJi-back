package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllByStudentClub(StudentClub studentClub);
    List<Receipt> findAllByStudentClubOrderByIdDesc(StudentClub studentClub);
    boolean existsByDateAndContent(Date date, String content);
    void deleteAllByStudentClub(StudentClub studentClub);
    int countByStudentClub(StudentClub studentClub);
    int countByStudentClubAndVerificationTrue(StudentClub studentClub);

    List<Receipt> findByStudentClubAndDateBetween(StudentClub studentClub, Date startDate, Date endDate);

    @Query("SELECT r FROM Receipt r WHERE r.studentClub = :studentClub AND FUNCTION('YEAR', r.date) = :year")
    List<Receipt> findByStudentClubAndYear(@Param("studentClub") StudentClub studentClub, @Param("year") int year);

    @Query(
        value = "SELECT * FROM receipt WHERE student_club_id = :studentClubId AND MATCH(content) AGAINST(:keyword IN BOOLEAN MODE) ORDER BY date DESC",
        nativeQuery = true
    )
    List<Receipt> findByStudentClubAndContent(
        @Param("studentClubId") Long studentClubId,
        @Param("keyword") String keyword
    );

}
