package com.example.tomyongji.auth;

import com.example.tomyongji.domain.admin.entity.Member;
import com.example.tomyongji.domain.admin.entity.President;
import com.example.tomyongji.domain.admin.repository.MemberRepository;
import com.example.tomyongji.domain.admin.repository.PresidentRepository;
import com.example.tomyongji.domain.auth.dto.ClubVerifyRequestDto;
import com.example.tomyongji.domain.auth.dto.LoginRequestDto;
import com.example.tomyongji.domain.auth.dto.UserRequestDto;
import com.example.tomyongji.domain.auth.entity.ClubVerification;
import com.example.tomyongji.domain.auth.entity.EmailVerification;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.jwt.JwtProvider;
import com.example.tomyongji.domain.auth.jwt.JwtToken;
import com.example.tomyongji.domain.auth.mapper.UserMapper;
import com.example.tomyongji.domain.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.domain.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.auth.service.UserServiceImpl;
import com.example.tomyongji.domain.receipt.entity.College;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.CollegeRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.global.error.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.tomyongji.global.error.ErrorMsg.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 클래스")
class UserServiceTest {

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
    void setUp() {
        college = College.builder()
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

    @Nested
    @DisplayName("signUp 메서드는")
    class Describe_signUp {

        @Nested
        @DisplayName("정상적인 회원가입 정보가 주어지면")
        class Context_with_valid_signup_info {

            EmailVerification emailVerification;
            ClubVerification clubVerification;

            @BeforeEach
            void setUp() {
                emailVerification = EmailVerification.builder()
                        .id(1L)
                        .email("eeeseohyun615@gmail.com")
                        .verificationCode("SSSSS1234")
                        .verificatedAt(LocalDateTime.now())
                        .build();

                clubVerification = ClubVerification.builder()
                        .id(1L)
                        .studentNum("60222024")
                        .verificatedAt(LocalDateTime.now())
                        .build();

                List<EmailVerification> emailList = new ArrayList<>();
                emailList.add(emailVerification);
                List<ClubVerification> clubList = new ArrayList<>();
                clubList.add(clubVerification);

                // given
                given(collegeRepository.findByCollegeName(studentRequestDto.getCollegeName()))
                        .willReturn(Optional.of(college));
                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(userRepository.findByUserId(studentRequestDto.getUserId()))
                        .willReturn(Optional.empty());
                given(emailVerificationRepository.findByEmailOrderByVerificatedAtDesc(studentRequestDto.getEmail()))
                        .willReturn(emailList);
                given(clubVerificationRepository.findByStudentNum(studentRequestDto.getStudentNum()))
                        .willReturn(clubList);
                given(userMapper.toUser(studentRequestDto, studentClub))
                        .willReturn(user);

                willAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    return savedUser;
                }).given(userRepository).save(any(User.class));

                willAnswer(invocation -> {
                    EmailVerification savedEmailVerification = invocation.getArgument(0);
                    savedEmailVerification.setUser(user);
                    return savedEmailVerification;
                }).given(emailVerificationRepository).save(any(EmailVerification.class));

                willAnswer(invocation -> {
                    ClubVerification savedClubVerification = invocation.getArgument(0);
                    savedClubVerification.setUser(user);
                    return savedClubVerification;
                }).given(clubVerificationRepository).save(any(ClubVerification.class));
            }

            @Test
            @DisplayName("회원가입에 성공하고 생성된 유저 ID를 반환한다")
            void it_returns_created_user_id() {
                // when
                long returnId = userService.signUp(studentRequestDto);

                // then
                assertThat(returnId).isEqualTo(user.getId());
                assertThat(emailVerification.getUser()).isEqualTo(user);
                assertThat(clubVerification.getUser()).isEqualTo(user);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 대학 소속의 회원가입 정보가 주어지면")
        class Context_with_nonexistent_college {

            UserRequestDto errorDto;

            @BeforeEach
            void setUp() {
                errorDto = UserRequestDto.builder()
                        .userId("tomyongji2024")
                        .name("투명지")
                        .password("*Tomyongji2024")
                        .role("STU")
                        .email("eeeseohyun615@gmail.com")
                        .collegeName("투명지대학")
                        .studentClubId(1L)
                        .studentNum("60222024")
                        .build();

                // given
                given(collegeRepository.findByCollegeName(errorDto.getCollegeName()))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_COLLEGE 예외를 던진다")
            void it_throws_not_found_college_exception() {
                // when & then
                assertThatThrownBy(() -> userService.signUp(errorDto))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_COLLEGE);
                        });

                then(collegeRepository).should().findByCollegeName(errorDto.getCollegeName());
                then(studentClubRepository).should(never()).findById(any());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회 소속의 회원가입 정보가 주어지면")
        class Context_with_nonexistent_student_club {

            UserRequestDto errorDto;

            @BeforeEach
            void setUp() {
                errorDto = UserRequestDto.builder()
                        .userId("tomyongji2024")
                        .name("투명지")
                        .password("*Tomyongji2024")
                        .role("STU")
                        .email("eeeseohyun615@gmail.com")
                        .collegeName("ICT융합대학")
                        .studentClubId(100L)
                        .studentNum("60222024")
                        .build();

                // given
                given(collegeRepository.findByCollegeName(errorDto.getCollegeName()))
                        .willReturn(Optional.of(college));
                given(studentClubRepository.findById(errorDto.getStudentClubId()))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> userService.signUp(errorDto))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_STUDENT_CLUB);
                        });

                then(studentClubRepository).should().findById(errorDto.getStudentClubId());
                then(userRepository).should(never()).findByUserId(any());
            }
        }

        @Nested
        @DisplayName("대학 안에 존재하지 않는 학생회 소속의 회원가입 정보가 주어지면")
        class Context_with_student_club_not_in_college {

            UserRequestDto errorDto;
            College otherCollege;
            StudentClub otherClub;

            @BeforeEach
            void setUp() {
                errorDto = UserRequestDto.builder()
                        .userId("tomyongji2024")
                        .name("투명지")
                        .password("*Tomyongji2024")
                        .role("STU")
                        .email("eeeseohyun615@gmail.com")
                        .collegeName("ICT융합대학")
                        .studentClubId(2L)
                        .studentNum("60222024")
                        .build();

                otherCollege = College.builder()
                        .id(2L)
                        .collegeName("투명지대학")
                        .build();

                otherClub = StudentClub.builder()
                        .id(2L)
                        .studentClubName("투명지 학생회")
                        .Balance(0)
                        .college(otherCollege)
                        .build();

                // given
                given(collegeRepository.findByCollegeName(errorDto.getCollegeName()))
                        .willReturn(Optional.of(college));
                given(studentClubRepository.findById(errorDto.getStudentClubId()))
                        .willReturn(Optional.of(otherClub));
            }

            @Test
            @DisplayName("NOT_HAVE_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_have_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> userService.signUp(errorDto))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_HAVE_STUDENT_CLUB);
                        });

                then(studentClubRepository).should().findById(errorDto.getStudentClubId());
                then(userRepository).should(never()).findByUserId(any());
            }
        }

        @Nested
        @DisplayName("이메일 인증을 하지 않은 회원가입 정보가 주어지면")
        class Context_with_unverified_email {

            @BeforeEach
            void setUp() {
                List<EmailVerification> emptyList = new ArrayList<>();

                // given
                given(collegeRepository.findByCollegeName(studentRequestDto.getCollegeName()))
                        .willReturn(Optional.of(college));
                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(userRepository.findByUserId(studentRequestDto.getUserId()))
                        .willReturn(Optional.empty());
                given(emailVerificationRepository.findByEmailOrderByVerificatedAtDesc(studentRequestDto.getEmail()))
                        .willReturn(emptyList);
            }

            @Test
            @DisplayName("NOT_VERIFY_EMAIL 예외를 던진다")
            void it_throws_not_verify_email_exception() {
                // when & then
                assertThatThrownBy(() -> userService.signUp(studentRequestDto))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_VERIFY_EMAIL);
                        });

                then(emailVerificationRepository).should()
                        .findByEmailOrderByVerificatedAtDesc(studentRequestDto.getEmail());
                then(clubVerificationRepository).should(never()).findByStudentNum(any());
            }
        }

        @Nested
        @DisplayName("소속 인증을 하지 않은 회원가입 정보가 주어지면")
        class Context_with_unverified_club {

            @BeforeEach
            void setUp() {
                EmailVerification emailVerification = EmailVerification.builder()
                        .id(1L)
                        .email("eeeseohyun615@gmail.com")
                        .verificationCode("SSSSS1234")
                        .verificatedAt(LocalDateTime.now())
                        .build();
                List<EmailVerification> emailList = new ArrayList<>();
                emailList.add(emailVerification);
                List<ClubVerification> emptyClubList = new ArrayList<>();

                // given
                given(collegeRepository.findByCollegeName(studentRequestDto.getCollegeName()))
                        .willReturn(Optional.of(college));
                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(userRepository.findByUserId(studentRequestDto.getUserId()))
                        .willReturn(Optional.empty());
                given(emailVerificationRepository.findByEmailOrderByVerificatedAtDesc(studentRequestDto.getEmail()))
                        .willReturn(emailList);
                given(clubVerificationRepository.findByStudentNum(studentRequestDto.getStudentNum()))
                        .willReturn(emptyClubList);
            }

            @Test
            @DisplayName("NOT_VERIFY_CLUB 예외를 던진다")
            void it_throws_not_verify_club_exception() {
                // when & then
                assertThatThrownBy(() -> userService.signUp(studentRequestDto))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_VERIFY_CLUB);
                        });

                then(clubVerificationRepository).should().findByStudentNum(studentRequestDto.getStudentNum());
                then(userMapper).should(never()).toUser(any(), any());
            }
        }
    }

    @Nested
    @DisplayName("checkUserIdDuplicate 메서드는")
    class Describe_checkUserIdDuplicate {

        @Nested
        @DisplayName("중복된 사용자 아이디가 주어지면")
        class Context_with_duplicate_user_id {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // given
                given(userRepository.existsByUserId(studentRequestDto.getUserId())).willReturn(true);

                // when
                Boolean isDuplicate = userService.checkUserIdDuplicate(studentRequestDto.getUserId());

                // then
                assertThat(isDuplicate).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("findUserIdByEmail 메서드는")
    class Describe_findUserIdByEmail {

        @Nested
        @DisplayName("등록된 이메일이 주어지면")
        class Context_with_registered_email {

            @Test
            @DisplayName("해당 사용자의 아이디를 반환한다")
            void it_returns_user_id() {
                // given
                given(userRepository.findByEmail(studentRequestDto.getEmail()))
                        .willReturn(Optional.of(user));

                // when
                String userId = userService.findUserIdByEmail(studentRequestDto.getEmail());

                // then
                assertThat(userId).isEqualTo(studentRequestDto.getUserId());
            }
        }
    }

    @Nested
    @DisplayName("verifyClub 메서드는")
    class Describe_verifyClub {

        @Nested
        @DisplayName("부원 소속 인증 정보가 주어지면")
        class Context_with_member_verification {

            ClubVerifyRequestDto clubVerifyRequestDto;
            Member member;
            ClubVerification clubVerification;

            @BeforeEach
            void setUp() {
                member = Member.builder()
                        .id(1L)
                        .studentNum(studentRequestDto.getStudentNum())
                        .name(studentRequestDto.getName())
                        .studentClub(studentClub)
                        .build();

                clubVerification = ClubVerification.builder()
                        .id(1L)
                        .studentNum("60222024")
                        .verificatedAt(LocalDateTime.now())
                        .build();

                clubVerifyRequestDto = ClubVerifyRequestDto.builder()
                        .clubId(studentClub.getId())
                        .studentNum(studentRequestDto.getStudentNum())
                        .role("STU")
                        .build();

                // given
                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(memberRepository.findByStudentNum(studentRequestDto.getStudentNum()))
                        .willReturn(Optional.of(member));
                given(clubVerificationRepository.save(any(ClubVerification.class)))
                        .willReturn(clubVerification);
            }

            @Test
            @DisplayName("인증에 성공하고 true를 반환한다")
            void it_returns_true() {
                // when
                Boolean isVerified = userService.verifyClub(clubVerifyRequestDto);

                // then
                assertThat(isVerified).isTrue();
            }
        }

        @Nested
        @DisplayName("회장 소속 인증 정보가 주어지면")
        class Context_with_president_verification {

            ClubVerifyRequestDto clubVerifyRequestDto;
            President president;
            ClubVerification clubVerification;

            @BeforeEach
            void setUp() {
                president = President.builder()
                        .id(1L)
                        .studentNum("60222024")
                        .name("투명지")
                        .build();

                clubVerification = ClubVerification.builder()
                        .id(1L)
                        .studentNum("60222024")
                        .verificatedAt(LocalDateTime.now())
                        .build();

                clubVerifyRequestDto = ClubVerifyRequestDto.builder()
                        .clubId(studentClub.getId())
                        .studentNum(studentRequestDto.getStudentNum())
                        .role("PRESIDENT")
                        .build();

                // given
                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(presidentRepository.findByStudentNum(studentRequestDto.getStudentNum()))
                        .willReturn(president);
                given(studentClubRepository.findByPresident(president))
                        .willReturn(Optional.of(studentClub));
                given(clubVerificationRepository.save(any(ClubVerification.class)))
                        .willReturn(clubVerification);
            }

            @Test
            @DisplayName("인증에 성공하고 true를 반환한다")
            void it_returns_true() {
                // when
                Boolean isVerified = userService.verifyClub(clubVerifyRequestDto);

                // then
                assertThat(isVerified).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("login 메서드는")
    class Describe_login {

        @Nested
        @DisplayName("유효한 로그인 정보가 주어지면")
        class Context_with_valid_credentials {

            LoginRequestDto loginRequestDto;
            UserDetails userDetails;
            Authentication authentication;
            JwtToken jwtToken;

            @BeforeEach
            void setUp() {
                loginRequestDto = LoginRequestDto.builder()
                        .userId("tomyongji2024")
                        .password("*Tomyongji2024")
                        .build();

                userDetails = new org.springframework.security.core.userdetails.User(
                        "tomyongji2024",
                        "*Tomyongji2024",
                        Collections.emptyList()
                );

                authentication = mock(Authentication.class);
                jwtToken = new JwtToken("bearer", "mockJwtToken", "");

                // given
                given(userRepository.findByUserId(loginRequestDto.getUserId()))
                        .willReturn(Optional.of(user));
                given(authentication.getPrincipal()).willReturn(userDetails);
                given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
                given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                        .willReturn(authentication);
                given(jwtProvider.generateToken(any(Authentication.class), eq(user.getId())))
                        .willReturn(jwtToken);
            }

            @Test
            @DisplayName("JWT 토큰을 생성하여 반환한다")
            void it_returns_jwt_token() {
                // when
                JwtToken token = userService.login(loginRequestDto);

                // then
                assertThat(token).isNotNull();
                assertThat(token.getAccessToken()).isEqualTo("mockJwtToken");
                assertThat(token.getGrantType()).isEqualTo("bearer");
            }
        }
    }
}