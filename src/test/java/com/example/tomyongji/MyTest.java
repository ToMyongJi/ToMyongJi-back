package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.my.service.MyService;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MyTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MyService myService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudentClubRepository studentClubRepository;

    @Autowired
    ClubVerificationRepository clubVerificationRepository;

    @Autowired
    PresidentRepository presidentRepository;
    @Autowired
    EmailVerificationRepository emailVerificationRepository;
    @Autowired
    PasswordEncoder encoder;
    private User user;


    @BeforeEach
    void setup() {
        //스마트시스템공과대학 학생회

        President president = President.builder()
            .studentNum("60211665")
            .name("박진형")
            .build();
        President savedPresident = presidentRepository.saveAndFlush(president);

        StudentClub studentClub = studentClubRepository.findById(30L).orElseThrow(()-> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));
        studentClub.setPresident(savedPresident);
        studentClubRepository.saveAndFlush(studentClub);

        ClubVerification clubVerification = ClubVerification.builder()
            .verificatedAt(LocalDateTime.now())
            .studentNum("60211665")
            .build();
        clubVerificationRepository.saveAndFlush(clubVerification);

        EmailVerification emailVerification = EmailVerification.builder()
            .verificatedAt(LocalDateTime.now())
            .email("jinhyoung9380@gmail.com")
            .build();
        emailVerificationRepository.saveAndFlush(emailVerification);

        user = User.builder()
            .id(1L)
            .userId("jinh9380")
            .name("박진형")
            .studentNum("60211665")
            .studentClub(studentClub)
            .collegeName("스마트시스템공과대학")
            .email("jinhyoung9380@gmail.com")
            .password(encoder.encode("password123!"))
            .role("PRESIDENT")
            .build();

        userRepository.saveAndFlush(user);
        user = userRepository.findByUserId(user.getUserId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    private void cleanupData() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            // 학생회 엔티티에서 president 참조 해제
            StudentClub studentClub = studentClubRepository.findByStudentClubName("스마트시스템공과대학 학생회");
            if (studentClub != null) {
                studentClub.setPresident(null);
                studentClubRepository.saveAndFlush(studentClub);
            }
            // User 엔티티에서 studentClub 참조 해제
            User user = userRepository.findByUserId("jinh9380").orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
            user.setStudentClub(null);
            userRepository.saveAndFlush(user);

            // 연관된 데이터 삭제
            presidentRepository.deleteByStudentNum("60211665");
            clubVerificationRepository.deleteByStudentNum("60211665");
            emailVerificationRepository.deleteByEmail("jinhyoung9380@gmail.com");
            userRepository.delete(user);

            memberRepository.deleteAllByStudentNum("60000001");
            memberRepository.deleteAllByStudentNum("60000002");
            return null;
        });
    }


    @AfterEach
    void clear() {
        cleanupData();
    }

    private String getToken() {
        LoginRequestDto loginRequest = new LoginRequestDto("jinh9380", "password123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<ApiResponse<JwtToken>> response = restTemplate.exchange(
            "http://localhost:8080/api/users/login",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<JwtToken>>() {}
        );
        System.out.println(response);
        return response.getBody().getData().getAccessToken(); // JWT 토큰 반환
    }

    @Test
    @DisplayName("유저 정보 조회 흐름 테스트")
    void testGetMyInfoFlow() throws Exception {
        //Given
        Long id = user.getId();
        assertThat(id).isNotNull();
        System.out.println("테스트 로그 - 유저명: " + user.getName());
        System.out.println("테스트 로그 - 유저 학생회: " + user.getStudentClub().getStudentClubName());
        String token = getToken();


        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> entity = new HttpEntity<>(headers);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", id);

        ResponseEntity<ApiResponse<MyDto>> response = restTemplate.exchange(
            "/api/my/{id}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<MyDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<MyDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("내 정보 조회에 성공했습니다.");
        assertThat(body.getData().getStudentNum()).isEqualTo("60211665");
        UserDetails currentUser = (UserDetails) new org.springframework.security.core.userdetails.User("jinh9380", "password123!", Collections.emptyList());
        CustomException exception = assertThrows(CustomException.class, () -> {
            myService.getMyInfo(1L, currentUser);
        });
        System.out.println("에러 이유: " + exception.getMessage());
    }

    @Test
    @DisplayName("멤버 정보 조회 흐름 테스트")
    void testGetMembersFlow() throws Exception {
        //Given
        Long id = user.getId();
        String token = getToken();

        Member member1 = Member.builder()
            .studentNum("60000001")
            .name("test member1")
            .studentClub(user.getStudentClub())
            .build();
        Member member2 = Member.builder()
            .studentNum("60000002")
            .name("test member2")
            .studentClub(user.getStudentClub())
            .build();

        memberRepository.saveAndFlush(member1);
        memberRepository.saveAndFlush(member2);

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", id);

        ResponseEntity<ApiResponse<List<MemberDto>>> response = restTemplate.exchange(
            "/api/my/members/{id}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<MemberDto>>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<MemberDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("소속 부원 조회에 성공했습니다.");
        //테스트를 위해 미리 넣어둔 부원이 0번을 차지하여 1번부터 확인
        assertThat(body.getData().get(1).getStudentNum()).isEqualTo("60000001");
        assertThat(body.getData().get(2).getStudentNum()).isEqualTo("60000002");

        //테스트 완료 후 삭제
        memberRepository.delete(member1);
        memberRepository.delete(member2);
    }

    @Test
    @DisplayName("멤버 저장 흐름 테스트")
    void testSaveMemberFlow() throws Exception {
        //Given
        SaveMemberDto saveMemberDto = SaveMemberDto.builder()
            .id(user.getId()) //회장의 인덱스 id
            .studentNum("60000001")
            .name("test name1")
            .build();
        String token = getToken();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<SaveMemberDto> entity = new HttpEntity<>(saveMemberDto, headers);
        //When
        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
            "/api/my/members",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<MemberDto>>() {}
        );
        //Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ApiResponse<MemberDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(201);
        assertThat(body.getStatusMessage()).isEqualTo("소속 부원 정보 저장에 성공했습니다.");
    }

    @Test
    @DisplayName("멤버 삭제 흐름 테스트")
    void testDeleteMemberFlow() throws Exception {
        // Given: 삭제할 멤버 데이터 준비
        Member deleteMember = Member.builder()
            .studentNum("60000003")
            .name("test name3")
            .studentClub(user.getStudentClub())
            .build();
        memberRepository.saveAndFlush(deleteMember);

        String deletedStudentNum = deleteMember.getStudentNum();
        String token = getToken();

        //When & Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deletedStudentNum", deletedStudentNum);

        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
            "/api/my/members/{deletedStudentNum}",
            HttpMethod.DELETE,
            entity,
            new ParameterizedTypeReference<ApiResponse<MemberDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<MemberDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("소속 부원 삭제에 성공했습니다.");
        assertThat(body.getData().getStudentNum()).isEqualTo("60000003");
    }
}