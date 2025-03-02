package com.example.tomyongji;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.*;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
    @Autowired
    private CollegeRepository collegeRepository;
    private UserRequestDto userRequestDto;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void clear() {
        new TransactionTemplate(transactionManager).execute(status -> {

            emailVerificationRepository.deleteByEmail("eeeseohyun@gmail.com");
            clubVerificationRepository.deleteByStudentNum("60222024");
            memberRepository.deleteAllByStudentNum("60222024");
            userRepository.deleteAllByStudentNum("60222024");
            //회장 소속인증용 정보 삭제
            clubVerificationRepository.deleteByStudentNum("60222025");
            StudentClub digital = studentClubRepository.findByStudentClubName("디지털콘텐츠학과 학생회");
            digital.setPresident(null);
            studentClubRepository.save(digital);
            presidentRepository.deleteByStudentNum("60222025");
            return null;
        });
    }
    @Test
    @DisplayName("회원가입 통합 테스트")
    void signUp() {
        //Given
        userRequestDto = UserRequestDto.builder()
                .userId("tomyongji")
                .name("투명지")
                .password("*Tomyongji2024")
                .role("STU")
                .email("eeeseohyun@gmail.com")
                .collegeName("인공지능소프트웨어융합대학")
                .studentClubId(25L)
                .studentNum("60222024")
                .build();
        EmailVerification emailVerification = EmailVerification.builder()
                .email("eeeseohyun@gmail.com")
                .verificationCode("SSSSS1234")
                .verificatedAt(LocalDateTime.now())
                .build();
        emailVerificationRepository.save(emailVerification);
        emailVerificationRepository.flush();
        StudentClub studentClub = studentClubRepository.findByStudentClubName("인공지능소프트웨어융합대학 학생회");
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
    }


    @DisplayName("유저 아이디 중복 검사 테스트")
    @Test
    void checkUserIdDuplicate(){
        //Given
        StudentClub studentClub = studentClubRepository.findByStudentClubName("인공지능소프트웨어융합대학 학생회");
        String userId = "newId";
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
        FindIdRequestDto findIdRequestDto = FindIdRequestDto.builder()
                .email("jinhyoung9380@gmail.com")
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
        assertThat(response.getBody().getData()).isEqualTo("jinh9380");
    }
    @DisplayName("부원 소속 인증 테스트")
    @Test
    void VerifyClubMember(){
        //Given
        StudentClub studentClub = studentClubRepository.findByStudentClubName("인공지능소프트웨어융합대학 학생회");
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(studentClub)
                .build();
        memberRepository.save(member);
        memberRepository.flush();

        ClubVerifyRequestDto clubVerifyRequestDto = ClubVerifyRequestDto.builder()
                .clubId(25L)
                .studentNum("60222024")
                .role("STU")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ClubVerifyRequestDto> entity = new HttpEntity<>(clubVerifyRequestDto, headers);

        //When
        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                "/api/users/clubVerify",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
        );

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isEqualTo(true);
    }
    @DisplayName("회장 소속 인증 테스트")
    @Test
    void VerifyClubPresident(){
        //Given
        President president = President.builder()
                .studentNum("60222025")
                .name("투명지")
                .build();
        presidentRepository.save(president);
        presidentRepository.flush();

        StudentClub digital = studentClubRepository.findByStudentClubName("디지털콘텐츠학과 학생회");
        digital.setPresident(president);
        studentClubRepository.save(digital);
        studentClubRepository.flush();

        ClubVerifyRequestDto clubVerifyRequestDto = ClubVerifyRequestDto.builder()
                .clubId(27L)
                .studentNum("60222025")
                .role("PRESIDENT")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ClubVerifyRequestDto> entity = new HttpEntity<>(clubVerifyRequestDto, headers);

        //When
        ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                "/api/users/clubVerify",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
        );

        //Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData()).isEqualTo(true);
    }

    @DisplayName("로그인")
    @Test
    void login(){
        //Given
        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .userId("jinh9380")
                .password("Jamespark1380@")
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
        userRepository.delete(user);
    }
}
