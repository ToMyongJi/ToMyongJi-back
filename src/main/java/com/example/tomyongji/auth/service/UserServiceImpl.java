package com.example.tomyongji.auth.service;


import com.example.tomyongji.admin.entity.MemberInfo;
import com.example.tomyongji.admin.entity.PresidentInfo;
import com.example.tomyongji.admin.repository.MemberInfoRepository;
import com.example.tomyongji.admin.repository.PresidentInfoRepository;
import com.example.tomyongji.auth.dto.CustomUserInfoDto;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtProvider;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PresidentInfoRepository presidentInfoRepository;
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

//    @Override
//    public Boolean verifyClub(Long clubId, Long userId) {
//        User user = this.userRepository.findById(userId).get();
//        String role =  user.getRole();
//        if(role.equals("PRESIDENT")){
//            long presidentInfoId= this.studentClubRepository.findById(clubId).get().getPresidentInfo().getId();
//            return this.presidentInfoRepository.findById(presidentInfoId).get().getStudentNum().equals(user.getStudentNum());
//        }
//        if(role.equals("STU")){
//           return this.memberInfoRepository.findByStudentNum(user.getStudentNum()).getStudentClub().getId().equals(clubId);
//        }
//        return null;
//    }
@Override
public Boolean verifyClub(Long clubId, Long userId) {
    Optional<User> optionalUser = this.userRepository.findById(userId);
    if (!optionalUser.isPresent()) {
        return false; // 사용자 정보를 찾을 수 없는 경우
    }
    User user = optionalUser.get();
    String role = user.getRole();

    Optional<StudentClub> optionalStudentClub = this.studentClubRepository.findById(clubId);
    if (!optionalStudentClub.isPresent()) {
        return false; // 학생회 정보를 찾을 수 없는 경우
    }
    StudentClub studentClub = optionalStudentClub.get();

    if (role.equals("PRESIDENT")) {
        Optional<PresidentInfo> optionalPresidentInfo = this.presidentInfoRepository.findById(studentClub.getPresidentInfo().getId());
        if (!optionalPresidentInfo.isPresent()) {
            return false; // 회장 정보가 없는 경우
        }
        PresidentInfo presidentInfo = optionalPresidentInfo.get();
        return presidentInfo.getStudentNum().equals(user.getStudentNum());
    }

    if (role.equals("STU")) {
        Optional<MemberInfo> optionalMemberInfo = Optional.ofNullable(this.memberInfoRepository.findByStudentNum(user.getStudentNum()));
        if (!optionalMemberInfo.isPresent()) {
            return false; // 회원 정보가 없는 경우
        }
        MemberInfo memberInfo = optionalMemberInfo.get();
        return memberInfo.getStudentClub().getId().equals(clubId);
    }

    return false; // 역할이 "PRESIDENT"나 "STU"가 아닌 경우
}

}
