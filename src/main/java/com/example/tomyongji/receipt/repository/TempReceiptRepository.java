package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.TempReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempReceiptRepository extends JpaRepository<TempReceipt, Long> {
}
