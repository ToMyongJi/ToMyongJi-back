package com.example.tomyongji.auth.repository;

import com.example.tomyongji.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email) ;
    Boolean existsByUserId(String userId);
    Optional<User> findByUserId(String email);
}
