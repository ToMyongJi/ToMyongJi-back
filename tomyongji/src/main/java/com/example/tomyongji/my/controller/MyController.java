package com.example.tomyongji.my.controller;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.service.MyService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my")
public class MyController {

    private final MyService myService;

    @Autowired
    public MyController(MyService myService) {
        this.myService = myService;
    }

    @Operation(summary = "내 정보 조회 api", description = "유저 아이디를 통해 유저 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<MyDto> getMyInfo(@PathVariable Long id) {
        MyDto myDto = myService.getMyInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(myDto);
    }

    @Operation(summary = "내 정보 수정 api", description = "유저 아이디와 학번으로 유저의 학번을 변경합니다.")
    @PostMapping("/{id}")
    public void updateMyInfo(@PathVariable Long id, @RequestParam String studentNum) {
        myService.updateMyInfo(id, studentNum);
    }

    @Operation(summary = "소속 부원 조회 api", description = "회장이 소속 부원들을 조회합니다.")
    @GetMapping("members/{id}") //자신의 아이디로 자기가 속한 학생회 조회
    public ResponseEntity<List<MemberDto>> getMembers(@PathVariable Long id) {
        List<MemberDto> memberDtos = myService.getMembers(id);
        return ResponseEntity.status(HttpStatus.OK).body(memberDtos);
    }

    @Operation(summary = "소속 부원 추가 api", description = "회장이 소속 부원 정보를 추가합니다.")
    @PostMapping("members/{id}") //자신의 아이디로 자기가 속한 학생회 조회
    public ResponseEntity<MemberDto> saveMember(@PathVariable Long id, @RequestBody MemberDto memberDto) {
        MemberDto memberDtoForSave =  myService.saveMember(id, memberDto);
        return ResponseEntity.status(HttpStatus.OK).body(memberDtoForSave);
    }

    @Operation(summary = "소속 부원 삭제 api", description = "회장이 소속 부원과 그 정보를 삭제합니다.")
    @DeleteMapping("members/{deleteId}")
    public void deleteMember(@PathVariable Long deleteId) {
        myService.deleteMember(deleteId);
    }

}
