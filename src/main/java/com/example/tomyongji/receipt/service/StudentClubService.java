package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class StudentClubService {
    private final StudentClubRepository studentClubRepository;
    public List<ClubDto> getAllStudentClub() {
        List<StudentClub> studentClubs = studentClubRepository.findAll();
        return clubDtoList(studentClubs);
    }

    public List<ClubDto> getStudentClubById(Long collegeId) {
        List<StudentClub> studentClubs = studentClubRepository.findAllByCollege_Id(collegeId);
        return clubDtoList(studentClubs);
    }

    private ClubDto convertToClubDto(StudentClub studentClub) {
        ClubDto clubDto = new ClubDto();
        clubDto.setStudentClubId(studentClub.getId());
        clubDto.setStudentClubName(studentClub.getStudentClubName());
        return clubDto;
    }
    private List<ClubDto> clubDtoList(List<StudentClub> studentClubs) {
        List<ClubDto> clubDtoList = new ArrayList<>();
        for (StudentClub studentClub : studentClubs) {
            clubDtoList.add(convertToClubDto(studentClub));
        }
        return clubDtoList;
    }
}
