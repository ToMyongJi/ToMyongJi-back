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
    @Autowired
    private final StudentClubRepository studentClubRepository;
    private final CollegeMapper collegeMapper;
    private final StudentClubRepository studentClubRepository;
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

    public List<CollegesDto> getAllCollegesAndClubs() {        // 모든 College를 조회합니다.
        return collegeRepository.findAll().stream()
            .map(college -> {
                // 각 College에 속한 StudentClub을 조회합니다.
                List<ClubDto> studentClubs = studentClubRepository.findAllByCollege_Id(college.getId()).stream()
                    .map(StudentClub::toDto)
                    .toList();

                // CollegesDto를 생성합니다.
                return new CollegesDto(
                    college.getId(),
                    college.getCollegeName(),
                    studentClubs
                );
            })
            .toList();

    }


}
