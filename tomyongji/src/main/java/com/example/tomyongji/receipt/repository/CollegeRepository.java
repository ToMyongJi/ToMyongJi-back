package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.College;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {

}
