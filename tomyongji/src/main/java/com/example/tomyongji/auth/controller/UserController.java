package com.example.tomyongji.auth.controller;

import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping("/api/users/signup")
    public ResponseEntity<Long> addUser(@Validated @RequestBody UserRequsetDto dto){
        User entity = modelMapper.map(dto, User.class);
        Long id = userService.join(entity);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}
