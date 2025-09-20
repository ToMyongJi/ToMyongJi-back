package com.example.tomyongji.auth.service;

import com.example.tomyongji.auth.dto.ClubVerifyRequestDto;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;

public interface UserService {
    Long signUp(UserRequestDto userRequestDto);

    Boolean checkUserIdDuplicate(String userId);

    JwtToken login(LoginRequestDto request);

    String findUserIdByEmail(String email);

    Boolean verifyClub(ClubVerifyRequestDto clubVerifyRequestDto);

    public void deleteUser(String userId);

}
