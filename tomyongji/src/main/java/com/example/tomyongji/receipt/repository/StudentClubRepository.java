package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentClubRepository extends JpaRepository<StudentClub, Long> {

    StudentClub findByUsers(User user);
}
