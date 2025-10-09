package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.TransferDto;
import com.example.tomyongji.receipt.dto.TransferRequestDto;
import com.example.tomyongji.receipt.service.CollegeService;
import com.example.tomyongji.receipt.service.StudentClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "학생회 및 대학 조회 api", description = "회원가입페이지 혹은 영수증 조회 페이지에서 학생회 및 대학을 조회할때 사용합니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class StudentClubController {

    private final StudentClubService studentClubService;

    @Operation(summary = "모든 학생회 조회 api", description = "모든 학생회를 조회할때 사용합니다.")
    @GetMapping("api/club")
    public ApiResponse<List<ClubDto>> getAllStudentClub() {
        List<ClubDto> clubs = studentClubService.getAllStudentClub();
        return new ApiResponse<>(200, "모든 학생회를 성공적으로 조회했습니다.", clubs); // 200 OK
    }

    @Operation(summary = "대학에 맞는 학생회 조회 api", description = "특정 대학에 속한 학생회를 조회합니다.")
    @GetMapping("api/club/{collegeId}")
    public ApiResponse<List<ClubDto>> getStudentClubById(
        @PathVariable("collegeId") Long collegeId) {
        List<ClubDto> clubs = studentClubService.getStudentClubById(collegeId);
        return new ApiResponse<>(200, "해당 단과대의 학생회를 성공적으로 조회했습니다.", clubs); // 200 OK
    }

    @Operation(summary = "학생회 이월/이전 api", description = "학생회 정보를 이월 합니다.")
    @PostMapping("api/club/transfer")
    public ApiResponse<TransferDto> transferStudentClub(
        @RequestBody TransferRequestDto request,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        TransferDto result = studentClubService.transferStudentClub(
            request,
            currentUser
        );
        return new ApiResponse<>(200, "학생회 이월이 성공적으로 완료되었습니다.", result);
    }
}
