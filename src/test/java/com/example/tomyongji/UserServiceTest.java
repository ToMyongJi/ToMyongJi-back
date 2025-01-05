package com.example.tomyongji;

import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtProvider;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.mapper.UserMapper;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.UserService;
import com.example.tomyongji.auth.service.UserServiceImpl;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    @Mock
    CollegeRepository collegeRepository;
    @Mock
    StudentClubRepository studentClubRepository;
    @Mock
    EmailVerificationRepository emailVerificationRepository;
    @Mock
    ClubVerificationRepository clubVerificationRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    PresidentRepository presidentRepository;
    @Mock
    PasswordEncoder encoder;
    @Mock
    UserMapper userMapper;
    @Mock
    JwtProvider jwtProvider;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    AuthenticationManagerBuilder authenticationManagerBuilder;

    UserRequestDto studentRequestDto;
    User user;
    College college;
    StudentClub studentClub;
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
        studentRequestDto = UserRequestDto.builder()
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
                .password(encoder.encode("*Tomyongji2024"))
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName(college.getCollegeName())
                .studentClub(studentClub)
                .studentNum("60222024")
                .build();
    }
    @Test
    @DisplayName("정상적인 회원가입 테스트")
    void signUpTest(){
        //given
        EmailVerification emailVerification = EmailVerification.builder()
                .id(1L)
                .email("eeeseohyun615@gmail.com")
                .verificationCode("SSSSS1234")
                .verificatedAt(LocalDateTime.now())
                .build();
        EmailVerification setUserEmailVerification = EmailVerification.builder()
                .id(1L)
                .email("eeeseohyun615@gmail.com")
                .verificationCode("SSSSS1234")
                .verificatedAt(LocalDateTime.now())
                .user(user)
                .build();
        ClubVerification clubVerification = ClubVerification.builder()
                .id(1L)
                .studentNum("60222024")
                .verificatedAt(LocalDateTime.now())
                .build();
        ClubVerification setUserClubVerification = ClubVerification.builder()
                .id(1L)
                .studentNum("60222024")
                .verificatedAt(LocalDateTime.now())
                .user(user)
                .build();


        when(collegeRepository.findByCollegeName(studentRequestDto.getCollegeName())).thenReturn(Optional.of(college));
        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(userRepository.findByUserId(studentRequestDto.getUserId())).thenReturn(Optional.empty());
        when(emailVerificationRepository.findByEmail(studentRequestDto.getEmail())).thenReturn(Optional.of(emailVerification));
        when(clubVerificationRepository.findByStudentNum(studentRequestDto.getStudentNum())).thenReturn(Optional.of(clubVerification));
        when(userMapper.toUser(studentRequestDto,studentClub)).thenReturn(user);
        doAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        }).when(userRepository).save(any(User.class));

        doAnswer(invocation -> {
            EmailVerification savedEmailVerification = invocation.getArgument(0);
            savedEmailVerification.setUser(user);
            return savedEmailVerification;
        }).when(emailVerificationRepository).save(any(EmailVerification.class));

        doAnswer(invocation -> {
            ClubVerification savedClubVerification = invocation.getArgument(0);
            savedClubVerification.setUser(user);
            return savedClubVerification;
        }).when(clubVerificationRepository).save(any(ClubVerification.class));

        //when
        long returnId = userService.signUp(studentRequestDto);
        //then
        assertThat(user.getId()).isEqualTo(returnId);
        assertThat(emailVerification.getUser()).isEqualTo(setUserEmailVerification.getUser());
        assertThat(clubVerification.getUser()).isEqualTo(setUserClubVerification.getUser());
    }

    @Test
    @DisplayName("사용자 아이디 중복 테스트")
    void checkUserEmailDuplicateTest(){
        //given
        when(userRepository.existsByUserId(studentRequestDto.getUserId())).thenReturn(true);
        //when
        Boolean EmailDuplicate = userService.checkUserIdDuplicate(studentRequestDto.getUserId());
        //then
        assertThat(EmailDuplicate).isEqualTo(true);
    }
    @Test
    @DisplayName("부원 소속 인증 테스트")
    void verifyClubMemberTest(){
        //given
        ClubVerification clubVerification = ClubVerification.builder()
                .id(1L)
                .studentNum("60222024")
                .verificatedAt(LocalDateTime.now())
                .build();
        Member member = Member.builder()
                        .id(1L)
                        .studentNum(studentRequestDto.getStudentNum())
                        .name(studentRequestDto.getName())
                        .studentClub(studentClub)
                        .build();

        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(presidentRepository.findByStudentNum(studentRequestDto.getStudentNum())).thenReturn(null);
        when(memberRepository.findByStudentNum(studentRequestDto.getStudentNum())).thenReturn(Optional.of(member));
        lenient().when(clubVerificationRepository.save(any(ClubVerification.class)))
                .thenReturn(clubVerification);
        //when
        Boolean isVerify = userService.verifyClub(studentClub.getId(), studentRequestDto.getStudentNum());
        //then
        assertThat(isVerify).isEqualTo(true);
    }
    @Test
    @DisplayName("회장 소속 인증 테스트")
    void verifyClubPresidentTest(){
        //given
        President president = President.builder()
                .id(1L)
                .studentNum("60222024")
                .name("투명지")
                .build();
        ClubVerification clubVerification = ClubVerification.builder()
                .id(1L)
                .studentNum("60222024")
                .verificatedAt(LocalDateTime.now())
                .build();
        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(presidentRepository.findByStudentNum(studentRequestDto.getStudentNum())).thenReturn(president);
        when(studentClubRepository.findByPresident(president)).thenReturn(Optional.of(studentClub));
        lenient().when(clubVerificationRepository.save(any(ClubVerification.class)))
                .thenReturn(clubVerification);
        //when
        Boolean isVerify = userService.verifyClub(studentClub.getId(), studentRequestDto.getStudentNum());
        //then
        assertThat(isVerify).isEqualTo(true);
    }
    @Test
    @DisplayName("로그인 테스트")
    void loginTest(){
        //given
        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                        .userId("tomyongji2024")
                        .password("*Tomyongji2024")
                        .build();
        when(userRepository.findByUserId(loginRequestDto.getUserId())).thenReturn(Optional.of(user));
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);

        when(jwtProvider.generateToken(any(Authentication.class), eq(user.getId())))
                .thenReturn(new JwtToken("bearer","mockJwtToken","")); // JwtToken mock 처리
        //when
        JwtToken token = userService.login(loginRequestDto);
        //then
        assertThat(token).isNotEqualTo(null);
        assertThat(token.getAccessToken()).isEqualTo("mockJwtToken");
    }
}
