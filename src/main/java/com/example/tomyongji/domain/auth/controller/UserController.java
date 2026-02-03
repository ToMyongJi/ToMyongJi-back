package com.example.tomyongji.domain.auth.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.auth.dto.ClubVerifyRequestDto;
import com.example.tomyongji.domain.auth.dto.FindIdRequestDto;
import com.example.tomyongji.domain.auth.dto.LoginRequestDto;
import com.example.tomyongji.domain.auth.dto.UserRequestDto;
import com.example.tomyongji.domain.auth.jwt.JwtToken;
import com.example.tomyongji.domain.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name="로그인, 회원가입 api", description = "로그인, 회원가입에 관련된 api입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입 api", description = "사용자가 회원가입하면, 유효성검사후 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody UserRequestDto userRequestDto){
        Long id = userService.signUp(userRequestDto);
        // 회원가입은 리소스 생성이므로 201 Created를 반환하는 것이 정석입니다.
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.onSuccess(id));
    }

    @Operation(summary = "유저 아이디 중복 검사 api", description = "사용자가 ID 중복검사를 누르면 중복 검사합니다. ")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserIdDuplicate(@PathVariable("userId") String userId){
        boolean isDuplicate = userService.checkUserIdDuplicate(userId);

        if(isDuplicate) {
            // 중복된 경우 409 Conflict 상태 코드를 사용하여 프론트가 즉시 에러로 인지하게 합니다.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.onFailure(409, "이미 존재하는 아이디입니다.", isDuplicate));
        } else {
            return ResponseEntity.ok(ApiResponse.onSuccess(isDuplicate));
        }
    }

    @Operation(summary = "로그인 api", description = "사용자가 로그인하면 토큰을 반환합니다")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtToken>> login(@Valid @RequestBody LoginRequestDto request) {
        JwtToken token = this.userService.login(request);
        return ResponseEntity.ok(ApiResponse.onSuccess(token));
    }

    @Operation(summary = "아이디 찾기 API", description = "이메일을 넣으면 ID를 찾아줍니다.")
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<String>> findUserIdByEmail(@RequestBody FindIdRequestDto findIdRequest){
        String id = userService.findUserIdByEmail(findIdRequest.getEmail());
        return ResponseEntity.ok(ApiResponse.onSuccess(id));
    }

    @Operation(summary = "소속 인증 API", description = "사용자 id와 학생회 id를 넣으면 소속인증을 합니다.")
    @PostMapping("/clubVerify")
    public ResponseEntity<ApiResponse<Boolean>> VerifyClub(@RequestBody ClubVerifyRequestDto clubVerifyDto) {
        boolean isClubVerify = userService.verifyClub(clubVerifyDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(isClubVerify));
    }

    @Operation(summary = "회원탈퇴 API", description = "사용자가 회원탈퇴를 합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal UserDetails currentUser) {
        String userId = currentUser.getUsername();
        userService.deleteUser(userId);
        // 데이터 반환이 없는 경우 null 처리를 포함한 오버로딩 메서드 활용
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

}
