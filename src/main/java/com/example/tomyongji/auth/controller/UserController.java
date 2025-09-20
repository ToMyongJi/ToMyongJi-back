package com.example.tomyongji.auth.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.auth.dto.ClubVerifyRequestDto;
import com.example.tomyongji.auth.dto.FindIdRequestDto;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<Long> signUp(@Valid @RequestBody UserRequestDto userRequestDto){
        Long id = userService.signUp(userRequestDto);
        return new ApiResponse<>(200,"회원가입에 성공하셨습니다. 사용자 index id: "+id,id);
    }
    @Operation(summary = "유저 아이디 중복 검사 api", description = "사용자가 ID 중복검사를 누르면 중복 검사합니다. ")
    @GetMapping("/{userId}")
    public ApiResponse<Boolean> checkUserIdDuplicate(@PathVariable("userId") String userId){
        boolean IsDuplicate = userService.checkUserIdDuplicate(userId);
        if(IsDuplicate==true) { return new ApiResponse(409,"이미 존재하는 아이디입니다.",IsDuplicate);}
        else { return new ApiResponse(200,"아이디 중복검사를 통과하였습니다.",IsDuplicate); }
    }
    @Operation(summary = "로그인 api", description = "사용자가 로그인하면 토큰을 반환합니다")
    @PostMapping("/login")
    public ApiResponse<JwtToken> login(@Valid @RequestBody LoginRequestDto request) {
        JwtToken token = this.userService.login(request);
        return new ApiResponse<>(200,"토큰을 성공적으로 발행했습니다.",token);
    }
    @Operation(summary = "아이디 찾기 API", description = "이메일을 넣으면 ID를 찾아줍니다.")
    @PostMapping("/find-id")
    public ApiResponse<String> findUserIdByEmail(@RequestBody FindIdRequestDto findIdRequest){
        String id = userService.findUserIdByEmail(findIdRequest.getEmail());
        return new ApiResponse<>(200,"ID를 성공적으로 반환합니다.",id);
    }

    @Operation(summary = "소속 인증 API", description = "사용자 id와 학생회 id를 넣으면  소속인증을 합니다.")
    @PostMapping("/clubVerify")
    public ApiResponse<Boolean> VerifyClub(@RequestBody ClubVerifyRequestDto clubVerifyDto) {
        boolean isClubVerify = userService.verifyClub(clubVerifyDto);
        return new ApiResponse(200,"소속인증을 성공적으로 마쳤습니다.",isClubVerify);
    }

    @Operation(summary = "회원탈퇴 API", description = "사용자가 회원탈퇴를 합니다.")
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteUser(@AuthenticationPrincipal UserDetails currentUser) {
        String userId = currentUser.getUsername();
        userService.deleteUser(userId);
        return new ApiResponse<>(200, "회원탈퇴가 완료되었습니다.", "탈퇴 완료");
    }
}
