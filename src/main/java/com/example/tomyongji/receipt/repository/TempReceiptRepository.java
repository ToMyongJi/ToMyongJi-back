package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.TempReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempReceiptRepository extends JpaRepository<TempReceipt, Long> {
    List<TempReceipt> findAllByBreakDownId(Long id);
    void deleteByBreakDownId(Long id);
}
