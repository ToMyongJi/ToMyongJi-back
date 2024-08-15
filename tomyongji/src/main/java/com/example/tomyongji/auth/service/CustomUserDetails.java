package com.example.tomyongji.auth.service;

import com.example.tomyongji.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 여기서 "ROLE_" 접두사를 제거
        return Arrays.stream(user.getRole().split(","))
                .map(SimpleGrantedAuthority::new) // "ROLE_" 접두사가 붙지 않음
                .collect(Collectors.toList());
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    // 기타 UserDetails 메소드 구현
}

