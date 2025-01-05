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
import com.example.tomyongji.validation.CustomException;
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

import static com.example.tomyongji.validation.ErrorMsg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @DisplayName("존재하지 않는 학생회 소속의 회원가입시 예외발생")
    void notFoundStudentClubSignUpTest(){
        //given
        UserRequestDto errorDto = UserRequestDto.builder()
                .userId("tomyongji2024")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName("ICT융합대학")
                .studentClubId(100L)
                .studentNum("60222024")
                .build();
        when(collegeRepository.findByCollegeName(errorDto.getCollegeName())).thenReturn(Optional.of(college));
        when(studentClubRepository.findById(errorDto.getStudentClubId())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->userService.signUp(errorDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(errorDto.getStudentClubId());
        verify(userRepository, never()).findByUserId(any());
    }
    @Test
    @DisplayName("존재하지 않는 대학 소속의 회원가입시 예외발생")
    void notFoundCollegeSignUpTest(){
        //given
        UserRequestDto errorDto = UserRequestDto.builder()
                .userId("tomyongji2024")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName("투명지대학")
                .studentClubId(1L)
                .studentNum("60222024")
                .build();
        when(collegeRepository.findByCollegeName(errorDto.getCollegeName())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->userService.signUp(errorDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_COLLEGE,exception.getMessage());
        verify(collegeRepository).findByCollegeName(errorDto.getCollegeName());
        verify(studentClubRepository, never()).findById(any());
    }
    @Test
    @DisplayName("대학안에 특정 학생회가 존재하지 않는 회원가입시 예외발생")
    void notHaveStudentClubSignUpTest(){
        //given
        UserRequestDto errorDto = UserRequestDto.builder()
                .userId("tomyongji2024")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName("ICT융합대학")
                .studentClubId(2L)
                .studentNum("60222024")
                .build();
        College otherCollege =College.builder()
                .id(2L)
                .collegeName("투명지대학")
                .build();
        StudentClub otherClub= StudentClub.builder()
                .id(2L)
                .studentClubName("투명지 학생회")
                .Balance(0)
                .college(otherCollege)
                .build();
        when(collegeRepository.findByCollegeName(errorDto.getCollegeName())).thenReturn(Optional.of(college));
        when(studentClubRepository.findById(errorDto.getStudentClubId())).thenReturn(Optional.of(otherClub));
        //when
        CustomException exception = assertThrows(CustomException.class,()->userService.signUp(errorDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_HAVE_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(errorDto.getStudentClubId());
        verify(userRepository, never()).findByUserId(any());
    }
    @Test
    @DisplayName("이메일 인증을 하지않는 회원가입시 예외발생")
    void notVerifyEmailSignUpTest(){

        when(collegeRepository.findByCollegeName(studentRequestDto.getCollegeName())).thenReturn(Optional.of(college));
        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(userRepository.findByUserId(studentRequestDto.getUserId())).thenReturn(Optional.empty());
        when(emailVerificationRepository.findByEmail(studentRequestDto.getEmail())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->userService.signUp(studentRequestDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_VERIFY_EMAIL,exception.getMessage());
        verify(emailVerificationRepository).findByEmail(studentRequestDto.getEmail());
        verify(clubVerificationRepository, never()).findByStudentNum(any());
    }
    @Test
    @DisplayName("소속 인증을 하지않는 회원가입시 예외발생")
    void notVerifyClubSignUpTest(){
        EmailVerification emailVerification = EmailVerification.builder()
                .id(1L)
                .email("eeeseohyun615@gmail.com")
                .verificationCode("SSSSS1234")
                .verificatedAt(LocalDateTime.now())
                .build();
        when(collegeRepository.findByCollegeName(studentRequestDto.getCollegeName())).thenReturn(Optional.of(college));
        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(userRepository.findByUserId(studentRequestDto.getUserId())).thenReturn(Optional.empty());
        when(emailVerificationRepository.findByEmail(studentRequestDto.getEmail())).thenReturn(Optional.of(emailVerification));
        when(clubVerificationRepository.findByStudentNum(studentRequestDto.getStudentNum())).thenReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,()->userService.signUp(studentRequestDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_VERIFY_CLUB,exception.getMessage());
        verify(clubVerificationRepository).findByStudentNum(studentRequestDto.getStudentNum());
        verify(userMapper, never()).toUser(any(),any());
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
    @DisplayName("id 찾기 테스트")
    void findUserIdByEmail(){
        //given
        when(userRepository.findByEmail(studentRequestDto.getEmail())).thenReturn(Optional.of(user));
        //when
        String userId = userService.findUserIdByEmail(studentRequestDto.getEmail());
        //then
        assertThat(userId).isEqualTo(studentRequestDto.getUserId() );
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
