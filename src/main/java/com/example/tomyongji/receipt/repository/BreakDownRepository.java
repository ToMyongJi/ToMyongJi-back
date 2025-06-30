package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.BreakDown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreakDownRepository extends JpaRepository<BreakDown, Long> {

}
