package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;

    public List<College> getAllCollege() {
        return this.collegeRepository.findAll();
    }
}
