package com.example.tomyongji.admin.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.dto.PresidentUpdateDto;
import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;

    @Autowired
    public AdminController(AdminService adminService, AdminMapper adminMapper) {
        this.adminService = adminService;
        this.adminMapper = adminMapper;
    }

    @Operation(summary = "학생회장 조회 api", description = "학생회 아이디를 통해 특정 학생회의 회장을 조회합니다.")
    @GetMapping("/president/{clubId}")
    public ApiResponse<PresidentDto> getPresident(@PathVariable Long clubId) {
        PresidentDto presidentDto = adminService.getPresident(clubId);
        return new ApiResponse<>(200, "학생회장 조회에 성공했습니다.", presidentDto);
    }

    @Operation(summary = "학생회장 저장 api", description = "학생회 아이디와 학번, 이름을 통해 특정 학생회의 회장 정보를 저장합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/president")
    public ApiResponse<PresidentDto> savePresident(@RequestBody PresidentUpdateDto presidentUpdateDto) {
        adminService.savePresident(presidentUpdateDto);
        PresidentDto presidentDto = adminMapper.toPresidentDto(presidentUpdateDto);
        return new ApiResponse<>(201, "학생회장 저장에 성공했습니다.", presidentDto);
    }

    @Operation(summary = "학생회장 수정 api", description = "학생회 아이디와 학번, 이름을 통해 특정 학생회의 회장 정보를 수정합니다.")
    @PatchMapping("/president")
    public ApiResponse<PresidentDto> updatePresident(@RequestBody PresidentUpdateDto presidentUpdateDto) {
        adminService.updatePresident(presidentUpdateDto);
        PresidentDto presidentDto = adminMapper.toPresidentDto(presidentUpdateDto);
        return new ApiResponse<>(200, "학생회장 수정에 성공했습니다.", presidentDto);
    }

    @Operation(summary = "소속 부원 조회 api", description = "학생회 아이디로 소속 부원을 조회합니다.")
    @GetMapping("/member/{clubId}")
    public ApiResponse<List<MemberDto>> getMembers(@PathVariable Long clubId) {
        List<MemberDto> users =  adminService.getMembers(clubId);
        return new ApiResponse<>(200, "소속 부원 조회에 성공했습니다.", users);
    }

    @Operation(summary = "소속 부원 저장 api", description = "학생회 아이디로 소속 부원 정보를 저장합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/member")
    public ApiResponse<MemberDto> saveMember(@RequestBody AdminSaveMemberDto memberDto) {
        adminService.saveMember(memberDto);
        return new ApiResponse<>(201, "소속 부원 저장에 성공했습니다.");
    }

    @Operation(summary = "소속 부원 삭제 api", description = "소속 부원 아이디로 소속 부원을 삭제합니다.")
    @DeleteMapping("/member/{memberId}")
    public ApiResponse<MemberDto> deleteMember(@PathVariable Long memberId) {
        MemberDto memberDto = adminService.deleteMember(memberId);
        return new ApiResponse<>(200, "소속 부원 삭제에 성공했습니다.", memberDto);
    }
}
