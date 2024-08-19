package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.service.CollegeService;
import com.example.tomyongji.receipt.service.StudentClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "학생회 및 대학 조회 api", description = "회원가입페이지 혹은 영수증 조회 페이지에서 학생회 및 대학을 조회할때 사용합니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class FindController {

    private final StudentClubService studentClubService;
    private final CollegeService collegeService;
    @Operation(summary = "모든 학생회 조회 api", description = "모든 학생회를 조회할때 사용합니다.")
    @GetMapping("api/club")
    public ResponseEntity<List<StudentClub>> getAllStudentClub() {
        List<StudentClub> club = studentClubService.getAllStudentClub();
        return ResponseEntity.status(HttpStatus.OK).body(club); // 200 OK
    }

    @Operation(summary = "모든 대학 조회 api", description = "모든 대학을 조회할때 사용합니다.")
    @GetMapping("api/college")
    public ResponseEntity<List<College>> getAllCollege() {
        List<College> colleges = collegeService.getAllCollege();
        return ResponseEntity.status(HttpStatus.OK).body(colleges);
    }

    @Operation(summary = "대학에 맞는 학생회 조회 api", description = "특정 대학에 속한 학생회를 조회합니다.")
    @GetMapping("api/club/{collegeId}")
    public ResponseEntity<List<StudentClub>> getStudentClubById(
        @PathVariable("collegeId") Long collegeId) {
        List<StudentClub> club = studentClubService.getStudentClubById(collegeId);
        return ResponseEntity.status(HttpStatus.OK).body(club); // 200 OK
    }
}
