package com.example.tomyongji;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.EmailDto;
import com.example.tomyongji.auth.dto.FindIdRequestDto;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    @Autowired
    private ClubVerificationRepository clubVerificationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PresidentRepository presidentRepository;
    @Autowired
    private StudentClubRepository studentClubRepository;
    private UserRequestDto userRequestDto;

    @Test
    @DisplayName("회원가입 통합 테스트")
    void signUp(){
        //Given
        userRequestDto = UserRequestDto.builder()
                .userId("tomyongji")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun@gmail.com")
                .collegeName("인공지능소프트웨어융합대학")
                .studentClubId(26L)
                .studentNum("60222024")
                .build();
        EmailVerification emailVerification = EmailVerification.builder()
                .email("eeeseohyun@gmail.com")
                .verificationCode("SSSSS1234")
                .verificatedAt(LocalDateTime.now())
                .build();
        emailVerificationRepository.save(emailVerification);
        emailVerificationRepository.flush();
        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();
        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("융합소프트웨어학부 학생회")
                .Balance(0)
                .college(college)
                .build();
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(studentClub)
                .build();
        memberRepository.save(member);
        memberRepository.flush();
        ClubVerification clubVerification = ClubVerification.builder()
                .studentNum("60222024")
                .verificatedAt(LocalDateTime.now())
                .build();
        clubVerificationRepository.save(clubVerification);
        clubVerificationRepository.flush();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserRequestDto> entity = new HttpEntity<>(userRequestDto, headers);
        //When
        ResponseEntity<ApiResponse<Long>> response = restTemplate.exchange(
                "/api/users/signup",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Long>>() {}
        );
        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isNotNegative();
        emailVerificationRepository.deleteAll();
        clubVerificationRepository.deleteAll();
        memberRepository.deleteAll();
        userRepository.deleteAll();
    }
    @DisplayName("유저 아이디 중복 검사 테스트")
    @Test
    void checkUserIdDuplicate(){
        //Given
        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();
        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .build();
        User user = User.builder()
                .id(1L)
                .userId("tomyongji2024")
                .name("투명지")
                .password(encoder.encode("*Tomyongji2024"))
                .role("STU")
                .email("eeeseohyun615@gmail.com")
                .collegeName("인공지능소프트웨어융합대학")
                .studentClub(studentClub)
                .studentNum("60222024")
                .build();
        String userId = "tomyongji";
        //When
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("userId", userId);

        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                "/api/users/{userId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {},
                uriVariables
        );

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isEqualTo(false);
    }
    @DisplayName("유저 아이디 찾기 테스트")
    @Test
    void findUserIdByEmail(){
        //Given
        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();
        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .build();
        User user = User.builder()
                .id(1L)
                .userId("tomyongji")
                .name("투명지")
                .password(encoder.encode("*Tomyongji2024"))
                .role("STU")
                .email("eeeseohyun@gmail.com")
                .collegeName("인공지능소프트웨어융합대학")
                .studentClub(studentClub)
                .studentNum("60222024")
                .build();
        userRepository.save(user);
        userRepository.flush();

        FindIdRequestDto findIdRequestDto = FindIdRequestDto.builder()
                .email("eeeseohyun@gmail.com")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FindIdRequestDto> entity = new HttpEntity<>(findIdRequestDto, headers);

        //When
        ResponseEntity<ApiResponse<String>> response = restTemplate.exchange(
                "/api/users/find-id",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<String>>() {}
        );

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isEqualTo("tomyongji");
        userRepository.deleteAll();
    }
    @DisplayName("부원 소속 인증 테스트")
    @Test
    void VerifyClubMember(){
        //Given
        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();
        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .build();
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(studentClub)
                .build();
        memberRepository.save(member);
        memberRepository.flush();


        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", 26L);
        uriVariables.put("studentNum","60222024");

        //When
        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                "/api/users/clubVerify/{clubId}/{studentNum}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {},
                uriVariables
        );

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isEqualTo(true);
        memberRepository.deleteAll();
        clubVerificationRepository.deleteAll();
    }
    @DisplayName("회장 소속 인증 테스트")
    @Test
    void VerifyClubPresident(){
        //Given
        President president = President.builder()
                .studentNum("60222024")
                .name("투명지")
                .build();
        presidentRepository.save(president);
        presidentRepository.flush();

        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();

        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .president(president)
                .build();
        studentClubRepository.save(studentClub);
        studentClubRepository.flush();

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", 26L);
        uriVariables.put("studentNum","60222024");

        //When
        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                "/api/users/clubVerify/{clubId}/{studentNum}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {},
                uriVariables
        );
        System.out.println("Response JSON: " + response.getBody());


        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isEqualTo(true);

        presidentRepository.deleteAll();
        clubVerificationRepository.deleteAll();
    }
    @DisplayName("로그인")
    @Test
    void login(){
        //Given
        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();

        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .build();
        User user = User.builder()
                .id(1L)
                .userId("tomyongji")
                .name("투명지")
                .password(encoder.encode("*Tomyongji2024"))
                .role("STU")
                .email("eeeseohyun@gmail.com")
                .collegeName("인공지능소프트웨어융합대학")
                .studentClub(studentClub)
                .studentNum("60222024")
                .build();
        userRepository.save(user);
        userRepository.flush();

        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .userId("tomyongji")
                .password("*Tomyongji2024")
                .build();

        //When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequestDto, headers);

        //When
        ResponseEntity<ApiResponse<JwtToken>> response = restTemplate.exchange(
                "/api/users/login",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<JwtToken>>() {}
        );

        System.out.println("Response JSON: " + response.getBody());

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isNotNull();
    }
    @DisplayName("이메일 전송")
    @Test
    void emailCheck(){
        //Given
        College college = College.builder()
                .id(6L)
                .collegeName("인공지능소프트웨어융합대학")
                .build();

        StudentClub studentClub = StudentClub.builder()
                .id(26L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .build();

        User user = User.builder()
                .id(1L)
                .userId("tomyongji")
                .name("투명지")
                .password(encoder.encode("*Tomyongji2024"))
                .role("STU")
                .email("eeeseohyun@gmail.com")
                .collegeName("인공지능소프트웨어융합대학")
                .studentClub(studentClub)
                .studentNum("60222024")
                .build();

        userRepository.save(user);
        userRepository.flush();

        EmailDto emailDto = EmailDto.builder()
                .email("eeeseohyun@gmail.com")
                .build();

        //When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailDto> entity = new HttpEntity<>(emailDto, headers);

        //When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/emailCheck",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<String>() {}
        );

        System.out.println("Response JSON: " + response.getBody());

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotEmpty();
        userRepository.deleteAll();
    }
}
