package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;

    public List<CollegeDto> getAllCollege() {
        List<College> collegeList = collegeRepository.findAll();
        return collegeDtoList(collegeList);
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
