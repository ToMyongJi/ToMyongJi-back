package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClubRepository extends JpaRepository<StudentClub, Long> {


    //StudentClub findByUsers(User user);

    List<StudentClub> findAllByCollege_Id(Long collegeId);

    Optional<StudentClub> findByPresident(President president);

    StudentClub findByStudentClubName(String clubName);

    @Modifying
    @Query("UPDATE StudentClub s SET s.verification = true WHERE s.id = :clubId")
    void updateVerificationById(@Param("clubId") Long clubId);

    @Modifying
    @Query("UPDATE StudentClub s SET s.verification = false WHERE s.id = :clubId")
    void updateVerificationToFalseById(@Param("clubId") Long clubId);
}
