package com.example.tomyongji.domain.admin.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.admin.dto.MemberDto;
import com.example.tomyongji.domain.admin.dto.PresidentDto;
import com.example.tomyongji.domain.admin.service.AdminService;
import com.example.tomyongji.domain.my.dto.AdminSaveMemberDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "어드민 api", description = "어드민이 학생회장 및 소속 부원 정보를 관리하는 API들입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "학생회장 조회 api", description = "학생회 아이디를 통해 특정 학생회의 회장을 조회합니다.")
    @GetMapping("/president/{clubId}")
    public ResponseEntity<ApiResponse<PresidentDto>> getPresident(@PathVariable("clubId") Long clubId) {
        PresidentDto presidentDto = adminService.getPresident(clubId);
        // 정적 메서드 onSuccess 사용 (메시지는 "요청에 성공했습니다."로 통일됨)
        return ResponseEntity.ok(ApiResponse.onSuccess(presidentDto));
    }

    @Operation(summary = "학생회장 저장 api", description = "학생회 아이디와 학번, 이름을 통해 특정 학생회의 회장 정보를 저장합니다.")
    @PostMapping("/president")
    public ResponseEntity<ApiResponse<PresidentDto>> savePresident(@RequestBody PresidentDto presidentDto) {
        PresidentDto response = adminService.savePresident(presidentDto);
        // 201 Created 응답은 onCreated 사용
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onCreated(response));
    }

    @Operation(summary = "학생회장 수정 api", description = "학생회 아이디와 학번, 이름을 통해 특정 학생회의 회장 정보를 수정합니다.")
    @PatchMapping("/president")
    public ResponseEntity<ApiResponse<PresidentDto>> updatePresident(@RequestBody PresidentDto presidentDto) {
        PresidentDto response = adminService.updatePresident(presidentDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "소속 부원 조회 api", description = "학생회 아이디로 소속 부원을 조회합니다.")
    @GetMapping("/member/{clubId}")
    public ResponseEntity<ApiResponse<List<MemberDto>>> getMembers(@PathVariable("clubId") Long clubId) {
        List<MemberDto> users = adminService.getMembers(clubId);
        return ResponseEntity.ok(ApiResponse.onSuccess(users));
    }

    @Operation(summary = "소속 부원 저장 api", description = "학생회 아이디로 소속 부원 정보를 저장합니다.")
    @PostMapping("/member")
    public ResponseEntity<ApiResponse<MemberDto>> saveMember(@RequestBody AdminSaveMemberDto memberDto) {
        MemberDto response = adminService.saveMember(memberDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onCreated(response));
    }

    @Operation(summary = "소속 부원 삭제 api", description = "소속 부원 아이디로 소속 부원을 삭제합니다.")
    @DeleteMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<MemberDto>> deleteMember(@PathVariable("memberId") Long memberId) {
        MemberDto memberDto = adminService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.onSuccess(memberDto));
    }
}
