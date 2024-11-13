package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.dto.CollegesDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
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

    public List<CollegesDto> getAllCollegesAndClubs() {
        List<CollegesDto> collegesAndClubs = new ArrayList<>();
        List<College> collegeList = collegeRepository.findAll();
        for(College college : collegeList){
            List<ClubDto> clubs = new ArrayList<>();
            for(StudentClub club : college.getStudentClubs()){
                clubs.add(club.toDto());
            }
            CollegesDto collegesDto = new CollegesDto(college.getId(),college.getCollegeName(),clubs);
            collegesAndClubs.add(collegesDto);
        }
        return collegesAndClubs;
    }

    private CollegeDto convertToCollegeDto(College college) {
        CollegeDto collegeDto = new CollegeDto();
        collegeDto.setName(college.getCollegeName());
        collegeDto.setId(college.getId());
        return collegeDto;
    }

    private List<CollegeDto> collegeDtoList(List<College> colleges) {
        List<CollegeDto> collegeDtoList = new ArrayList<>();
        for (College college : colleges) {
            collegeDtoList.add(convertToCollegeDto(college));
        }
        return collegeDtoList;
    }

}
