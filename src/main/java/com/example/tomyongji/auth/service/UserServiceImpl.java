package com.example.tomyongji.auth.service;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtProvider;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.mapper.UserMapper;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
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
    private final UserMapper userMapper;
    @Override
    public Long signUp(UserRequestDto dto) {
        User user =createUser(dto);
        Optional<User> valiUser = userRepository.findByUserId(user.getUserId());
        if (valiUser.isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이메일입니다.");
        }
        // 비밀번호 해시 처리
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return userRepository.findByUserId(dto.getUserId()).get().getId();
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
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 없습니다.: " + email));
    }


    @Override
    public Boolean verifyClub(Long clubId, String studentNum) { //학생회 아이디, 유저 학번

        Optional<StudentClub> optionalStudentClub = this.studentClubRepository.findById(clubId);
        if (!optionalStudentClub.isPresent()) {
            return false; // 학생회 정보를 찾을 수 없는 경우
        }
        StudentClub studentClub = optionalStudentClub.get(); //학생회
        President president =this.presidentInfoRepository.findByStudentNum(studentNum);
        if (president==null) { //회장의 학번이 아니라면
            Member member = this.memberInfoRepository.findByStudentNum(studentNum)
                .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER, 400));
            if (member!=null) return true;
        }else {
            StudentClub userClub = studentClubRepository.findByPresident(president);
            if (userClub!=null) return true;
        }
            return false;
        }

    @Override
    public User createUser(UserRequestDto dto) {
        System.out.println(dto.getStudentClubId());
        StudentClub studentClub = studentClubRepository.findById(dto.getStudentClubId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid student club ID"));
        User user = userMapper.toUser(dto,studentClub);
        if(user.getStudentClub()==null){
            user.setStudentClub(studentClub);
        }

        return user;
    }

}
