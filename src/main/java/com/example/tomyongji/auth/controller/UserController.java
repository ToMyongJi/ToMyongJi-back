package com.example.tomyongji.auth.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.auth.dto.FindIdRequestDto;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.service.EmailService;
import com.example.tomyongji.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ThreadPoolExecutor;

@Tag(name="로그인, 회원가입 api", description = "로그인, 회원가입에 관련된 api입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    @Operation(summary = "회원가입 api", description = "사용자가 회원가입하면, 유효성검사후 회원가입합니다.")
    @PostMapping("/signup")
    public ApiResponse<Long> addUser(@Valid @RequestBody UserRequsetDto dto){
        TypeMap<UserRequsetDto, User> typeMap = modelMapper.getTypeMap(UserRequsetDto.class, User.class);
        if (typeMap == null) {
            typeMap = modelMapper.createTypeMap(UserRequsetDto.class, User.class)
                    .addMappings(mapper -> mapper.skip(User::setId));
        }
        User entity = typeMap.map(dto);
        Long id = userService.join(entity);
        return new ApiResponse<>(200,"회원가입에 성공하셨습니다.",id);
    }
    @Operation(summary = "유저 아이디 중복 검사 api", description = "사용자가 ID 중복검사를 누르면 중복 검사합니다. ")
    @GetMapping("/{userId}")
    public ApiResponse<Boolean> checkUserIdDuplicate(@PathVariable String userId){
        boolean IsDuplicate = userService.checkUserIdDuplicate(userId);
        if(IsDuplicate==true) { return new ApiResponse(409,"이미 존재하는 아이디입니다.",IsDuplicate);}
        else { return new ApiResponse(200,"아이디 중복검사를 통과하였습니다.",IsDuplicate); }
    }
    @Operation(summary = "로그인 api", description = "사용자가 로그인하면 토큰을 반환합니다")
    @PostMapping("/login")
    public ApiResponse<JwtToken> getMemberProfile(@Valid @RequestBody LoginRequestDto request) {
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
    @GetMapping("/clubVerify/{clubId}/{studentNum}")
    public ApiResponse<Boolean> VerifyClub(@PathVariable Long clubId, @PathVariable String studentNum) {
        boolean isClubVerify = userService.verifyClub(clubId,studentNum);
        return new ApiResponse(200,"소속인증을 성공적으로 마쳤습니다.",isClubVerify);
    }
}
