package com.example.tomyongji;

import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.mapper.UserMapper;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.UserService;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;
    @Mock
    UserRepository userRepository;
    @Mock
    CollegeRepository collegeRepository;
    @Mock
    StudentClubRepository studentClubRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    PresidentRepository presidentRepository;
    @Mock
    PasswordEncoder encoder;
    @Mock
    UserMapper userMapper;
    UserRequestDto userRequestDto;
    StudentClub studentClub;
    College college;
    User user;

    @BeforeEach
    void beforeEach(){

        college =College.builder()
                .id(1L)
                .collegeName("ICT융합대학")
                .build();

        studentClub = StudentClub.builder()
                .id(1L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .build();

        userRequestDto = UserRequestDto.builder()
                .userId("tomyongji2024")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName(college.getCollegeName())
                .studentClubId(studentClub.getId())
                .studentNum("60222024")
                .build();
        user = User.builder()
                .id(1L)
                .userId("tomyongji2024")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName(college.getCollegeName())
                .studentClub(studentClub)
                .studentNum("60222024")
                .build();
    }
    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest(){
        //given
        when(userMapper.toUser(userRequestDto,studentClub)).thenReturn(user);
        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(userRepository.findByUserId(userRequestDto.getUserId())).thenReturn(Optional.of(user));
        //when
        long returnId = userService.signUp(userRequestDto);
        //then
//        assertEquals()
        assertThat(user.getStudentNum()).isEqualTo(userRequestDto.getStudentNum());
    }

    @Test
    @DisplayName("사용자 아이디 중복 테스트")
    void checkUserEmailDuplicateTest(){
        //given
        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);
        //when
        Boolean EmailDuplicate = userService.checkUserIdDuplicate(userRequestDto.getUserId());
        //then
        assertThat(EmailDuplicate).isEqualTo(true);
    }
    @Test
    @DisplayName("부원 소속 인증 테스트")
    void verifyClubMemberTest(){
        //given
        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);

        Member member = new Member();
        member.setName(userRequestDto.getName());
        member.setStudentNum(userRequestDto.getStudentNum());
        member.setStudentClub(studentClub);
        memberRepository.save(member);
        //when
        Boolean isVerify = userService.verifyClub(studentClub.getId(),userRequestDto.getStudentNum());
        //then
        assertThat(isVerify).isEqualTo(true);
    }
    @Test
    @DisplayName("회장 소속 인증 테스트")
    void verifyClubPresidentTest(){
        //given
        President president = new President();
        president.setName(userRequestDto.getName());
        president.setStudentNum(userRequestDto.getStudentNum());
        presidentRepository.save(president);
        studentClub.setPresident(president);
        studentClubRepository.save(studentClub);

        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole("PRESIDENT");
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);

        //when
        Boolean isVerify = userService.verifyClub(studentClub.getId(),userRequestDto.getStudentNum());
        //then
        assertThat(isVerify).isEqualTo(true);
    }
    @Test
    @DisplayName("로그인 테스트")
    void loginTest(){
        //given
        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId(userRequestDto.getUserId());
        loginRequestDto.setPassword(userRequestDto.getPassword());
        //when
        JwtToken token = userService.login(loginRequestDto);
        //then
        assertThat(token).isNotEqualTo(null);
    }
}
