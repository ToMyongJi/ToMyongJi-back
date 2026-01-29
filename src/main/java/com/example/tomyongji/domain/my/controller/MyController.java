package com.example.tomyongji.domain.my.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.admin.dto.MemberDto;
import com.example.tomyongji.domain.my.dto.MyDto;
import com.example.tomyongji.domain.my.dto.SaveMemberDto;
import com.example.tomyongji.domain.my.service.MyService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyController {

    private final MyService myService;


    @Operation(summary = "내 정보 조회 api", description = "유저 아이디를 통해 유저 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MyDto>> getMyInfo(
        @PathVariable("id") Long id,
        @AuthenticationPrincipal UserDetails currentUser) {
        MyDto myDto = myService.getMyInfo(id, currentUser);
        return ResponseEntity.ok(
            ApiResponse.onSuccess(myDto)
        );
    }

//    @Operation(summary = "내 정보 수정 api", description = "유저 아이디와 학번으로 유저의 학번을 변경합니다.")
//    @PostMapping("/{id}")
//    public void updateMyInfo(@PathVariable Long id, @RequestParam String studentNum) {
//        myService.updateMyInfo(id, studentNum);
//    }

    @Operation(summary = "소속 부원 조회 api", description = "회장이 소속 부원들을 조회합니다.")
    @GetMapping("members/{id}") //자신의 아이디로 자기가 속한 학생회 조회
    public ResponseEntity<ApiResponse<List<MemberDto>>> getMembers(
        @PathVariable("id") Long id,
        @AuthenticationPrincipal UserDetails currentUser) {
        List<MemberDto> memberDtos = myService.getMembers(id, currentUser);
        return ResponseEntity.ok(
            ApiResponse.onSuccess(memberDtos)
        );
    }

    @Operation(summary = "소속 부원 추가 api", description = "회장이 소속 부원 정보를 추가합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("members") //회장이 자신의 유저 아이디로 자기가 속한 학생회 조회
    public ResponseEntity<ApiResponse<MemberDto>> saveMember(
        @RequestBody SaveMemberDto saveMemberDto,
        @AuthenticationPrincipal UserDetails currentUser) {
        myService.saveMember(saveMemberDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.onCreated(null));
    }

    @Operation(summary = "소속 부원 삭제 api", description = "회장이 소속 부원과 그 정보를 삭제합니다.")
    @DeleteMapping("members/{deletedStudentNum}") //삭제할 멤버 아이디를 통한 삭제
    public ResponseEntity<ApiResponse<MemberDto>> deleteMember(
        @PathVariable("deletedStudentNum") String deletedStudentNum,
        @AuthenticationPrincipal UserDetails currentUser) {
        MemberDto memberDto = myService.deleteMember(deletedStudentNum, currentUser);
        return ResponseEntity.ok(
            ApiResponse.onSuccess(memberDto)
        );
    }
}
