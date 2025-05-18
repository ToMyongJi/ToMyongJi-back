package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_COLLEGE;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.CustomUserDetails;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.CollegesDto;
import com.example.tomyongji.receipt.dto.ReceiptByStudentClubDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumMap;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReceiptTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentClubRepository studentClubRepository;
    @Autowired
    private PresidentRepository presidentRepository;
    @Autowired
    private ClubVerificationRepository clubVerificationRepository;
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    @Autowired
    private PasswordEncoder encoder;

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

            receiptRepository.deleteAllByStudentClub(studentClub);
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
    @DisplayName("특정 영수증 작성 흐름 테스트")
    void testSaveReceiptFlow() {
        //Given
        String userId = user.getUserId();
        ReceiptCreateDto receiptCreateDto = ReceiptCreateDto.builder()
            .userId(userId)
            .date(new Date())
            .content("테스트")
            .deposit(1000)
            .build();
        String token = getToken();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<ReceiptCreateDto> entity = new HttpEntity<>(receiptCreateDto, headers);

        ResponseEntity<ApiResponse<ReceiptDto>> response = restTemplate.exchange(
            "/api/receipt",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<ReceiptDto>>() {}
        );
        //Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ApiResponse<ReceiptDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(201);
        assertThat(body.getStatusMessage()).isEqualTo("영수증을 성공적으로 작성했습니다.");
    }

    @Test
    @DisplayName("모든 영수증 조회 흐름 테스트")
    void testGetAllReceiptsFlow() throws Exception {

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ApiResponse<List<ReceiptDto>>> response = restTemplate.exchange(
            "/api/receipt",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<ReceiptDto>>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<ReceiptDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("모든 영수증을 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("특정 학생회 영수증 조회 흐름 테스트")
    void testGetReceiptsByClubFlow() throws Exception {
        //Given
        Long id = user.getId();
        String token = getToken();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", id);

        ResponseEntity<ApiResponse<ReceiptByStudentClubDto>> response = restTemplate.exchange(
            "/api/receipt/club/{id}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<ReceiptByStudentClubDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<ReceiptByStudentClubDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("해당 학생회의 영수증들을 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("특정 학생회 영수증 조회 학생용 흐름 테스트")
    void testGetReceiptsByClubForStudentFlow() throws Exception {
        //Given
        Long clubId = user.getStudentClub().getId();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", clubId);

        ResponseEntity<ApiResponse<List<ReceiptDto>>> response = restTemplate.exchange(
            "/api/receipt/club/{clubId}/student",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<ReceiptDto>>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<ReceiptDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("해당 학생회의 영수증들을 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("특정 영수증 조회 흐름 테스트")
    void testGetReceiptById() throws Exception {
        //Given
        Receipt receipt = Receipt.builder()
            .content("영수증 테스트")
            .deposit(1000)
            .studentClub(user.getStudentClub())
            .build();
        receiptRepository.save(receipt);
        long receiptId = receipt.getId();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("receiptId", receiptId);

        ResponseEntity<ApiResponse<ReceiptDto>> response = restTemplate.exchange(
            "/api/receipt/{receiptId}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<ReceiptDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<ReceiptDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("영수증을 성공적으로 조회했습니다.");
        assertThat(body.getData().getReceiptId()).isEqualTo(receiptId);
    }

    @Test
    @DisplayName("특정 영수증 삭제 흐름 테스트")
    void testDeleteReceiptById() throws Exception {
        //Given
        Receipt receipt = Receipt.builder()
            .content("영수증 테스트")
            .deposit(1000)
            .studentClub(user.getStudentClub())
            .build();
        receiptRepository.save(receipt);
        long receiptId = receipt.getId();

        String token = getToken();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("receiptId", receiptId);

        ResponseEntity<ApiResponse<ReceiptDto>> response = restTemplate.exchange(
            "/api/receipt/{receiptId}",
            HttpMethod.DELETE,
            entity,
            new ParameterizedTypeReference<ApiResponse<ReceiptDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<ReceiptDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("영수증을 성공적으로 삭제했습니다.");
        assertThat(body.getData().getReceiptId()).isEqualTo(receiptId);
    }

    @Test
    @DisplayName("특정 영수증 수정 흐름 테스트")
    void testUpdateReceiptFlow() {
        //Given
        String token = getToken();
        Receipt receipt = Receipt.builder()
            .content("영수증 테스트")
            .deposit(1000)
            .studentClub(user.getStudentClub())
            .build();
        receiptRepository.save(receipt);
        ReceiptDto receiptUpdateDto = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .date(new Date())
            .content("수정된 내용")
            .deposit(2000)
            .build();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<ReceiptDto> entity = new HttpEntity<>(receiptUpdateDto, headers);
        ResponseEntity<ApiResponse<ReceiptDto>> response = restTemplate.exchange(
            "/api/receipt",
            HttpMethod.PUT,
            entity,
            new ParameterizedTypeReference<ApiResponse<ReceiptDto>>() {}
        );
        //Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<ReceiptDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getData().getContent()).isEqualTo(receiptUpdateDto.getContent());
        assertThat(body.getStatusMessage()).isEqualTo("영수증을 성공적으로 수정했습니다.");
        assertThat(body.getData().getContent()).isEqualTo(receiptUpdateDto.getContent());
    }

    @Test
    @DisplayName("모든 대학 조회 흐름 테스트")
    void testGetAllCollegesAndClubsFlow() throws Exception {

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ApiResponse<List<CollegesDto>>> response = restTemplate.exchange(
            "/api/collegesAndClubs",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<CollegesDto>>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<CollegesDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("모든 단과대를 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("모든 학생회 조회 흐름 테스트")
    void testGetAllStudentClubsFlow() throws Exception {

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ApiResponse<List<ClubDto>>> response = restTemplate.exchange(
            "/api/club",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<ClubDto>>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<ClubDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("모든 학생회를 성공적으로 조회했습니다.");
    }

    @Test
    @DisplayName("대학에 맞는 학생회 조회 흐름 테스트")
    void testGetAllClubsByCollegeFlow() throws Exception {
        //Given
        Long collegeId = 26L; //인공지능소프트웨어융합대학

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("collegeId", collegeId);

        ResponseEntity<ApiResponse<List<ClubDto>>> response = restTemplate.exchange(
            "/api/club/{collegeId}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<ClubDto>>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<ClubDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("해당 단과대의 학생회를 성공적으로 조회했습니다.");
    }
}