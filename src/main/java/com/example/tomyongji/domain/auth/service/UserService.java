package com.example.tomyongji.domain.auth.service;

import com.example.tomyongji.domain.auth.dto.ClubVerifyRequestDto;
import com.example.tomyongji.domain.auth.dto.LoginRequestDto;
import com.example.tomyongji.domain.auth.dto.UserRequestDto;
import com.example.tomyongji.domain.auth.jwt.JwtToken;

public interface UserService {
    Long signUp(UserRequestDto userRequestDto);

    Boolean checkUserIdDuplicate(String userId);

    JwtToken login(LoginRequestDto request);

    String findUserIdByEmail(String email);

    Boolean verifyClub(ClubVerifyRequestDto clubVerifyRequestDto);

    public void deleteUser(String userId);

}
