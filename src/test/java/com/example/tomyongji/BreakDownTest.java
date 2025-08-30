package com.example.tomyongji;

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
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BreakDownTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentClubRepository studentClubRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private PresidentRepository presidentRepository;

    @Autowired
    private ClubVerificationRepository clubVerificationRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private User user;
    private StudentClub studentClub;
    private MockMultipartFile pdfFile;

    @BeforeEach
    void setUp() throws IOException {
        President president = President.builder()
                .studentNum("60221317")
                .name("정우주")
                .build();
        President savedPresident = presidentRepository.saveAndFlush(president);

        studentClub = studentClubRepository.findById(30L)
                .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));
        studentClub.setPresident(savedPresident);
        studentClubRepository.saveAndFlush(studentClub);

        ClubVerification clubVerification = ClubVerification.builder()
                .verificatedAt(LocalDateTime.now())
                .studentNum("60221317")
                .build();
        clubVerificationRepository.saveAndFlush(clubVerification);

        EmailVerification emailVerification = EmailVerification.builder()
                .verificatedAt(LocalDateTime.now())
                .email("test@example.com")
                .verificationCode("TEST1234")
                .build();
        emailVerificationRepository.saveAndFlush(emailVerification);

        user = User.builder()
                .userId("testUser")
                .name("테스트유저")
                .studentNum("60221317")
                .studentClub(studentClub)
                .collegeName("스마트시스템공과대학")
                .email("test@example.com")
                .password(encoder.encode("password123!"))
                .role("PRESIDENT")
                .build();
        userRepository.saveAndFlush(user);

        loadPdf();
    }

    private void loadPdf() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("breakdown_test.pdf");
            byte[] pdfBytes = StreamUtils.copyToByteArray(resource.getInputStream());

            pdfFile = new MockMultipartFile(
                    "file",
                    "breakdown_test.pdf",
                    "application/pdf",
                    pdfBytes
            );
        } catch (IOException e) {
            throw new RuntimeException("테스트 리소스에서 PDF 파일 로드 실패: " + e.getMessage(), e);
        }
    }

    @AfterEach
    void clear() {
        new TransactionTemplate(transactionManager).execute(status -> {
            receiptRepository.deleteAllByStudentClub(studentClub);

            studentClub.setPresident(null);
            studentClubRepository.saveAndFlush(studentClub);

            userRepository.deleteAllByStudentNum("60221317");
            clubVerificationRepository.deleteByStudentNum("60221317");
            emailVerificationRepository.deleteByEmail("test@example.com");
            presidentRepository.deleteByStudentNum("60221317");

            return null;
        });
    }

    private String getAuthToken() {
        LoginRequestDto loginRequest = new LoginRequestDto("testUser", "password123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequest, headers);

        try {
            ResponseEntity<ApiResponse<JwtToken>> response = restTemplate.exchange(
                    "/api/users/login",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<JwtToken>>() {}
            );
            return response.getBody().getData().getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException("토큰 발급 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("PDF 파싱 성공 통합테스트")
    void parsePdfHttpTest() throws Exception {
        // Given
        String userId = user.getUserId();
        String authToken = getAuthToken();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(pdfFile.getBytes()) {
            @Override
            public String getFilename() {
                return pdfFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);
        body.add("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<ApiResponse<List<ReceiptDto>>> response = restTemplate.exchange(
                "/api/breakdown/parse",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<List<ReceiptDto>>>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ApiResponse<List<ReceiptDto>> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertThat(responseBody.getStatusCode()).isEqualTo(200);
        assertThat(responseBody.getStatusMessage()).isEqualTo("PDF 파싱을 성공적으로 완료했습니다.");
    }
}