package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentClubRepository extends JpaRepository<StudentClub, Long> {


    //StudentClub findByUsers(User user);
    List<StudentClub> findAllByCollege_Id(Long collegeId);

    StudentClub findByPresident(President president);

}
