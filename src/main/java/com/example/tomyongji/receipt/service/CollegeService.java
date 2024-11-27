package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.dto.CollegesDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.CollegeMapper;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import java.util.ArrayList;

import com.example.tomyongji.receipt.repository.StudentClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {

    @Autowired
    private final CollegeRepository collegeRepository;
    private final CollegeMapper collegeMapper;
    public List<CollegesDto> getAllCollegesAndClubs() {

        return collegeRepository.findAll().stream()
                .map(college -> new CollegesDto(
                        college.getId(),
                        college.getCollegeName(),
                        college.getStudentClubs().stream()
                                .map(StudentClub::toDto)
                                .toList()
                ))
                .toList();

    }

}
