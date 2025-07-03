package com.example.tomyongji.admin.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.BreakDown;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "학생회장 조회 api", description = "학생회 아이디를 통해 특정 학생회의 회장을 조회합니다.")
    @GetMapping("/president/{clubId}")
    public ApiResponse<PresidentDto> getPresident(@PathVariable("clubId") Long clubId) {
        PresidentDto presidentDto = adminService.getPresident(clubId);
        return new ApiResponse<>(200, "학생회장 조회에 성공했습니다.", presidentDto);
    }

    @Operation(summary = "학생회장 저장 api", description = "학생회 아이디와 학번, 이름을 통해 특정 학생회의 회장 정보를 저장합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/president")
    public ApiResponse<PresidentDto> savePresident(@RequestBody PresidentDto presidentDto) {
        PresidentDto response = adminService.savePresident(presidentDto);
        return new ApiResponse<>(201, "학생회장 저장에 성공했습니다.", response);
    }

    @Operation(summary = "학생회장 수정 api", description = "학생회 아이디와 학번, 이름을 통해 특정 학생회의 회장 정보를 수정합니다.")
    @PatchMapping("/president")
    public ApiResponse<PresidentDto> updatePresident(@RequestBody PresidentDto presidentDto) {
        PresidentDto response = adminService.updatePresident(presidentDto);
        return new ApiResponse<>(200, "학생회장 수정에 성공했습니다.", response);
    }

    @Operation(summary = "소속 부원 조회 api", description = "학생회 아이디로 소속 부원을 조회합니다.")
    @GetMapping("/member/{clubId}")
    public ApiResponse<List<MemberDto>> getMembers(@PathVariable("clubId") Long clubId) {
        List<MemberDto> users =  adminService.getMembers(clubId);
        return new ApiResponse<>(200, "소속 부원 조회에 성공했습니다.", users);
    }

    // 수정
    @Operation(summary = "소속 부원 저장 api", description = "학생회 아이디로 소속 부원 정보를 저장합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/member")
    public ApiResponse<MemberDto> saveMember(@RequestBody AdminSaveMemberDto memberDto) {
        MemberDto response = adminService.saveMember(memberDto);
        return new ApiResponse<>(201, "소속 부원 저장에 성공했습니다.", response);
    }

    @Operation(summary = "소속 부원 삭제 api", description = "소속 부원 아이디로 소속 부원을 삭제합니다.")
    @DeleteMapping("/member/{memberId}")
    public ApiResponse<MemberDto> deleteMember(@PathVariable("memberId") Long memberId) {
        MemberDto memberDto = adminService.deleteMember(memberId);
        return new ApiResponse<>(200, "소속 부원 삭제에 성공했습니다.", memberDto);
    }

    @Operation(summary = "거래내역서 승인 요청 전체 조회 api", description = "거래내역서 승인 요청 전체를 조회합니다.")
    @GetMapping("/breakdowns")
    public ApiResponse<List<BreakDownDto>> getAllBreakDowns() {
        List<BreakDownDto> breakDownDtoList = adminService.getAllBreakDowns();
        return new ApiResponse<>(200, "거래 내역서 승인 요청 전체 조회에 성공했습니다.", breakDownDtoList);
    }

    @Operation(summary = "특정 거래내역서 승인 요청 조회 api", description = "거래내역서 아이디를 통해 승인 요청을 조회합니다.")
    @GetMapping("/breakdowns/{breakDownId}")
    public ApiResponse<BreakDownDto> getBreakDown(@PathVariable("breakDownId") Long breakDownId) {
        BreakDownDto breakDownDto = adminService.getBreakDown(breakDownId);
        return new ApiResponse<>(200, "특정 거래 내역서 승인 요청 조회에 성공했습니다.", breakDownDto);
    }

    @Operation(summary = "거래내역서 승인 api", description = "거래내역서 아이디를 통해 영수증 업로드를 승인합니다.")
    @PostMapping("/breakdowns/approval/{breakDownId}")
    public ApiResponse<List<ReceiptDto>> approveBreakDown(@PathVariable("breakDownId") Long breakDownId) {
        List<ReceiptDto> receiptDtoList = adminService.approveBreakDown(breakDownId);
        return new ApiResponse<>(200, "영수증 업로드를 승인에 승인했습니다.", receiptDtoList);
    }
}
