package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentClubRepository extends JpaRepository<StudentClub, Long> {

    List<StudentClub> findAllByCollege_Id(Long collegeId);
}
