package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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
import org.springframework.core.io.ByteArrayResource;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OCRTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private StudentClub studentClub;
    private MockMultipartFile testImageFile;

    @BeforeEach
    void setUp() throws IOException {
        // President 설정
        President president = President.builder()
                .studentNum("60221317")
                .name("테스트유저")
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

        byte[] imageBytes = loadTestImageFromResources();
        testImageFile = new MockMultipartFile(
                "file",
                "OCRTest.jpg",
                "image/jpeg",
                imageBytes
        );
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

    private byte[] loadTestImageFromResources() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/OCRTest.jpg")) {
            if (inputStream == null) {
                throw new IOException("테스트 이미지 파일 'OCRTest.jpg'을 찾을 수 없습니다.");
            }
            return inputStream.readAllBytes();
        }
    }

    private byte[] createTestImage() {
        return "test image content".getBytes();
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

    private void performSuccessfulOCRUploadTest(String authToken, String userId, MockMultipartFile file) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/ocr/upload/" + userId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then - 실제 OCR 서비스의 응답을 그대로 검증하는 부분
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        assertThat(responseBody.get("statusCode")).isEqualTo(201);
        assertThat(responseBody.get("statusMessage")).isEqualTo("영수증을 성공적으로 업로드했습니다.");
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        assertNotNull(data);

        assertNotNull(data.get("content"));
        assertNotNull(data.get("withdrawal"));

        List<Receipt> savedReceipts = receiptRepository.findAllByStudentClub(studentClub);
        assertFalse(savedReceipts.isEmpty());
    }

    @Test
    @DisplayName("OCR 영수증 업로드 성공 통합테스트 - 실제 OCR 서비스 사용")
    void uploadOCRReceiptTest() throws Exception {
        // Given
        String authToken = getAuthToken();
        String userId = user.getUserId();

        // When & Then
        performSuccessfulOCRUploadTest(authToken, userId, testImageFile);
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식 OCR 처리 실패 테스트")
    void processInvalidFileFormatTest() throws Exception {
        // Given
        String authToken = getAuthToken();
        String userId = user.getUserId();

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "invalid file content".getBytes()
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(invalidFile.getBytes()) {
            @Override
            public String getFilename() {
                return invalidFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/ocr/upload/" + userId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();

        if (response.getBody() != null) {
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(response.getBody(), Map.class);
                String message = (String) errorResponse.get("message");
                assertThat(message).isNotNull();
                assertThat(message.contains("지원하지 않는 파일 형식") || message.contains("OCR 처리 중 예외가 발생했습니다")).isTrue();
            } catch (Exception e) {
                assertThat(response.getBody()).contains("txt");
            }
        }
    }

    @Test
    @DisplayName("권한이 없는 사용자의 OCR 요청 실패 테스트")
    void unauthorizedOCRRequestTest() throws Exception {
        // Given
        String userId = user.getUserId();

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "receipt.jpg",
                "image/jpeg",
                createTestImage()
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/ocr/upload/" + userId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("JPEG 파일 확장자 OCR 처리 테스트")
    void processJPEGFileTest() throws Exception {
        // Given
        String authToken = getAuthToken();
        String userId = user.getUserId();

        MockMultipartFile jpegFile = new MockMultipartFile(
                "file",
                "receipt.jpeg",
                "image/jpeg",
                loadTestImageFromResources()
        );

        // When & Then - 실제 OCR 서비스 호출
        performSuccessfulOCRUploadTest(authToken, userId, jpegFile);
    }

    @Test
    @DisplayName("빈 파일 OCR 처리 실패 테스트")
    void processEmptyFileTest() throws Exception {
        // Given
        String authToken = getAuthToken();
        String userId = user.getUserId();

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(emptyFile.getBytes()) {
            @Override
            public String getFilename() {
                return emptyFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/ocr/upload/" + userId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()).isTrue();
    }
}