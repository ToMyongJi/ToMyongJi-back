package com.example.tomyongji.auth.controller;

import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.service.EmailService;
import com.example.tomyongji.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping("/signup")
    public ResponseEntity<Long> addUser(@Valid @RequestBody UserRequsetDto dto){
        TypeMap<UserRequsetDto, User> typeMap = modelMapper.createTypeMap(UserRequsetDto.class, User.class)
                .addMappings(mapper -> mapper.skip(User::setId));
        User entity = typeMap.map(dto);
        Long id = userService.join(entity);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
    @GetMapping("/{user-id}")
    public ResponseEntity<Boolean> checkUserIdDuplicate(@PathVariable String userId){
        return ResponseEntity.ok(userService.checkUserIdDuplicate(userId));
    }
    @PostMapping("/login")
    public JwtToken getMemberProfile(@Valid @RequestBody LoginRequestDto request) {
        JwtToken token = this.userService.login(request);
        return token;
    }
    @PostMapping("/test")
    public String test() {
        return "success";
    }
//    @GetMapping("/api/users/role")
}
