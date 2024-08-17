package com.example.tomyongji.auth.controller;

import com.example.tomyongji.auth.dto.FindIdRequestDto;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
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

@Tag(name="로그인, 회원가입 api", description = "로그인, 회원가입에 관련된 api입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    //    @Operation(summary = "회원가입 api", description = "사용자가 회원가입하면, 유효성검사후 회원가입합니다.")
//    @PostMapping("/signup")
//    public ResponseEntity<Long> addUser(@Valid @RequestBody UserRequsetDto dto){
//        TypeMap<UserRequsetDto, User> typeMap = modelMapper.createTypeMap(UserRequsetDto.class, User.class)
//                .addMappings(mapper -> mapper.skip(User::setId));
//        User entity = typeMap.map(dto);
//        Long id = userService.join(entity);
//        return ResponseEntity.status(HttpStatus.OK).body(id);
//    }
    @Operation(summary = "회원가입 api", description = "사용자가 회원가입하면, 유효성검사후 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Long> addUser(@Valid @RequestBody UserRequsetDto dto) {
        TypeMap<UserRequsetDto, User> typeMap = modelMapper.getTypeMap(UserRequsetDto.class, User.class);
        if (typeMap == null) {
            typeMap = modelMapper.createTypeMap(UserRequsetDto.class, User.class)
                    .addMappings(mapper -> mapper.skip(User::setId));
        }
        User entity = typeMap.map(dto);
        Long id = userService.join(entity);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @Operation(summary = "유저 아이디 중복 검사 api", description = "사용자가 ID 중복검사를 누르면 중복 검사합니다. ")
    @GetMapping("/{userId}")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@PathVariable String userId) {
        return ResponseEntity.ok(userService.checkUserIdDuplicate(userId));
    }

    @Operation(summary = "로그인 api", description = "사용자가 로그인하면 토큰을 반환합니다")
    @PostMapping("/login")
    public JwtToken getMemberProfile(@Valid @RequestBody LoginRequestDto request) {
        JwtToken token = this.userService.login(request);
        return token;
    }

    @Operation(summary = "테스트 API, front 사용 X", description = "ADMIN 권한 사람들만 들어갈수 있는 api로 테스트용입니다. 프론트 사용 X")
    @PostMapping("/test")
    public String test() {
        return "success";
    }

    //    @GetMapping("/api/users/role")
    @Operation(summary = "아이디 찾기 API", description = "이메일을 넣으면 ID를 찾아줍니다.")
    @PostMapping("/find-id")
    public ResponseEntity<String> findUserIdByEmail(@RequestBody FindIdRequestDto findIdRequest) {
        return ResponseEntity.ok(userService.findUserIdByEmail(findIdRequest.getEmail()));
    }

    @Operation(summary = "소속 인증 API", description = "사용자 id와 학생회 id를 넣으면  소속인증을 합니다.")
    @GetMapping("/clubVerify/{clubId}/{userId}")
    public ResponseEntity<Boolean> VerifyClub(@PathVariable Long clubId, @PathVariable Long userId) {
        return ResponseEntity.ok(userService.verifyClub(clubId, userId));
    }
}
