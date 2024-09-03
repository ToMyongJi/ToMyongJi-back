package com.example.tomyongji.auth.service;

import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;

public interface UserService {
    Long join(User entity);

    Boolean checkUserIdDuplicate(String userId);

    JwtToken login(LoginRequestDto request);

    String findUserIdByEmail(String email);

    Boolean verifyClub(Long clubId, String studentNum);

    User createUser(UserRequsetDto dto);
}
