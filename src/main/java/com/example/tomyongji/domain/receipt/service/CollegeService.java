package com.example.tomyongji.domain.receipt.service;

import com.example.tomyongji.domain.receipt.mapper.CollegeMapper;
import com.example.tomyongji.domain.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.domain.receipt.repository.CollegeRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.dto.CollegesDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {
    private final CollegeRepository collegeRepository;
    private final StudentClubRepository studentClubRepository;
    private final CollegeMapper collegeMapper;
    private final StudentClubMapper studentClubMapper;
//    public List<CollegesDto> getAllCollegesAndClubs() {
//
//        return collegeRepository.findAll().stream()
//                .map(college -> new CollegesDto(
//                        college.getId(),
//                        college.getCollegeName(),
//                        college.getStudentClubs().stream()
//                                .map(StudentClub::toDto)
//                                .toList()
//                ))
//                .toList();
//
//    }

    public List<CollegesDto> getAllCollegesAndClubs() {
        return collegeRepository.findAll().stream()
            // "어드민"인 college 제외
            .filter(college -> !"어드민".equals(college.getCollegeName()))
            .map(college -> {
                List<ClubDto> studentClubs = studentClubRepository.findAllByCollege_Id(college.getId()).stream()
                    .map(studentClubMapper::toClubDto)
                    .toList();

                return new CollegesDto(
                    college.getId(),
                    college.getCollegeName(),
                    studentClubs
                );
            })
            .toList();
    }



}
