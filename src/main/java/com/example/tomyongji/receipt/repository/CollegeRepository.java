package com.example.tomyongji.receipt.repository;

import com.example.tomyongji.receipt.entity.College;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {
    @Override
    Optional<College> findById(Long aLong);

    Optional<College> findByCollegeName(String collegeName);
}
