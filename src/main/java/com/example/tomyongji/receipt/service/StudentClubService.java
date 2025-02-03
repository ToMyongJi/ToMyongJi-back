package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class StudentClubService {
    private final StudentClubRepository studentClubRepository;
    private final StudentClubMapper studentClubMapper;
    public List<ClubDto> getAllStudentClub() {
        List<StudentClub> studentClubs = studentClubRepository.findAll();

        // 특정 StudentClub을 제외 (예: 특정 이름 제외)
        List<StudentClub> filteredClubs = studentClubs.stream()
            .filter(club -> !club.getStudentClubName().equals("어드민")) // 제외할 조건
            .collect(Collectors.toList());

        return clubDtoList(filteredClubs);
    }


    public List<ClubDto> getStudentClubById(Long collegeId) {
        List<StudentClub> studentClubs = studentClubRepository.findAllByCollege_Id(collegeId);
        return clubDtoList(studentClubs);
    }
    private List<ClubDto> clubDtoList(List<StudentClub> studentClubs) {
        List<ClubDto> clubDtoList = new ArrayList<>();
        for (StudentClub studentClub : studentClubs) {
            clubDtoList.add(studentClubMapper.toClubDto(studentClub));
        }
        return clubDtoList;
    }
}
