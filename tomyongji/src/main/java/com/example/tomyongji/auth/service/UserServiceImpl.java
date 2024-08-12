package com.example.tomyongji.auth.service;


import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {
    private final UserRepository memberRepository;
    private final PasswordEncoder encoder;


    @Override
    public Long join(User user) {
        Optional<User> valiUser = memberRepository.findByEmail(user.getEmail());
        if(valiUser.isPresent()) {
//            throw new ValidateUserException("This member email is already exist. " + user.getEmail());
        }
        // 비밀번호 해시 처리
        user.setPassword(encoder.encode(user.getPassword()));
        memberRepository.save(user);
        return user.getId();
    }
}
