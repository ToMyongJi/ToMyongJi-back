package com.example.tomyongji.auth.service;


import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtProvider;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MemberRepository memberInfoRepository;
    private final PresidentRepository presidentInfoRepository;
    private final StudentClubRepository studentClubRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;

    @Override
    public Long join(User user) {
        Optional<User> valiUser = userRepository.findByUserId(user.getUserId());
        if (valiUser.isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }
        // 비밀번호 해시 처리
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return user.getId();
    }

    @Override
    public Boolean checkUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Override
    public JwtToken login(LoginRequestDto dto) {
        String userId = dto.getUserId();
        String password = dto.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증된 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // JwtToken 생성
        JwtToken jwtToken = jwtProvider.generateToken(authentication, this.userRepository.findByUserId(dto.getUserId()).get().getId());
        return jwtToken;
    }

    @Override
    public String findUserIdByEmail(String email) {
        return this.userRepository.findByEmail(email).map(User::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }


    @Override
    public Boolean verifyClub(Long clubId, String studentNum) {

        Optional<StudentClub> optionalStudentClub = this.studentClubRepository.findById(clubId);
        if (!optionalStudentClub.isPresent()) {
            return false; // 학생회 정보를 찾을 수 없는 경우
        }
        StudentClub studentClub = optionalStudentClub.get();


        Optional<President> optionalPresidentInfo = Optional.ofNullable(this.presidentInfoRepository.findByStudentNum(studentNum));
        if (!optionalPresidentInfo.isPresent()) {
            Optional<Member> optionalMemberInfo = Optional.ofNullable(this.memberInfoRepository.findByStudentNum(studentNum));
            if (!optionalMemberInfo.isPresent()) {
                return false; // 회원 정보가 없는 경우
            } else {
                if (optionalPresidentInfo.get().getStudentClub().getId().equals(clubId)) {
                    return true;
                }
            }
        }
        return false; // 역할이 "PRESIDENT"나 "STU"가 아닌 경우
    }

    @Override
    public User createUser(UserRequsetDto dto) {
        System.out.println(dto.getStudentClubId());
        StudentClub studentClub = studentClubRepository.findById(dto.getStudentClubId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid student club ID"));

        User user = User.builder()
                .userId(dto.getUserId())
                .name(dto.getName())
                .studentNum(dto.getStudentNum())
                .college(dto.getCollege())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .role(dto.getRole())
                .studentClub(studentClub) // StudentClub 객체 설정
                .build();

        return user;
    }

}
