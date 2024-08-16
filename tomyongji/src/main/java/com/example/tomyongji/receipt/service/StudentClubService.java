package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class StudentClubService {
    private final StudentClubRepository studentClubRepository;
    public List<StudentClub> getAllStudentClub() {
        return this.studentClubRepository.findAll();
    }

    public List<StudentClub> getStudentClubById(Long collegeId) {
        return this.studentClubRepository.findAllByCollege_Id(collegeId);
    }
}
