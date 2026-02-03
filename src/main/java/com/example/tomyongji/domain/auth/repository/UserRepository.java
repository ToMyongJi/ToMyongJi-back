package com.example.tomyongji.domain.auth.repository;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email) ;
    Boolean existsByUserId(String userId);
    Optional<User> findByUserId(String userId);
    Optional<User> findById(long Id);
    List<User> findByStudentClubAndRole(StudentClub studentClub, String role);
    User findFirstByStudentClubAndRole(StudentClub studentClub, String president);
    User findByStudentNum(String studentNum);

    void deleteAllByStudentNum(String studentNum);
}
