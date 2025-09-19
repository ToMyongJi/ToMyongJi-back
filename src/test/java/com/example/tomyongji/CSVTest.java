package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.example.tomyongji.receipt.dto.CsvExportDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class CSVTest {

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
    private User anotherUser;
    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private MockMultipartFile csvFile;

    @BeforeEach
    void setUp() {
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

        President anotherPresident = President.builder()
                .studentNum("60221318")
                .name("다른유저")
                .build();
        President savedAnotherPresident = presidentRepository.saveAndFlush(anotherPresident);

        anotherStudentClub = studentClubRepository.findById(31L)
                .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));
        anotherStudentClub.setPresident(savedAnotherPresident);
        studentClubRepository.saveAndFlush(anotherStudentClub);

        ClubVerification anotherClubVerification = ClubVerification.builder()
                .verificatedAt(LocalDateTime.now())
                .studentNum("60221318")
                .build();
        clubVerificationRepository.saveAndFlush(anotherClubVerification);

        EmailVerification anotherEmailVerification = EmailVerification.builder()
                .verificatedAt(LocalDateTime.now())
                .email("another@example.com")
                .verificationCode("TEST5678")
                .build();
        emailVerificationRepository.saveAndFlush(anotherEmailVerification);

        anotherUser = User.builder()
                .userId("anotherUser")
                .name("다른유저")
                .studentNum("60221318")
                .studentClub(anotherStudentClub)
                .collegeName("스마트시스템공과대학")
                .email("another@example.com")
                .password(encoder.encode("password123!"))
                .role("PRESIDENT")
                .build();
        userRepository.saveAndFlush(anotherUser);

        String csvContent = "date,content,deposit,withdrawal\n" +
                "2024-01-15,카페 결제,0,5000\n" +
                "2024-01-16,입금,10000,0\n" +
                "2024-01-17,문구점 결제,0,3000";

        csvFile = new MockMultipartFile(
                "file",
                "receipts.csv",
                "text/csv",
                csvContent.getBytes()
        );
    }

    @AfterEach
    void clear() {
        new TransactionTemplate(transactionManager).execute(status -> {
            receiptRepository.deleteAllByStudentClub(studentClub);
            receiptRepository.deleteAllByStudentClub(anotherStudentClub);

            studentClub.setPresident(null);
            anotherStudentClub.setPresident(null);
            studentClubRepository.saveAndFlush(studentClub);
            studentClubRepository.saveAndFlush(anotherStudentClub);

            userRepository.deleteAllByStudentNum("60221317");
            userRepository.deleteAllByStudentNum("60221318");
            clubVerificationRepository.deleteByStudentNum("60221317");
            clubVerificationRepository.deleteByStudentNum("60221318");
            emailVerificationRepository.deleteByEmail("test@example.com");
            emailVerificationRepository.deleteByEmail("another@example.com");
            presidentRepository.deleteByStudentNum("60221317");
            presidentRepository.deleteByStudentNum("60221318");

            return null;
        });
    }

    private String getAuthToken(String userId, String password) {
        LoginRequestDto loginRequest = new LoginRequestDto(userId, password);

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

    private String getAuthToken() {
        return getAuthToken("testUser", "password123!");
    }

    @Test
    @DisplayName("CSV 업로드 성공 통합테스트")
    void uploadCsvTest() throws Exception {
        // Given
        long userIndexId = user.getId();
        String authToken = getAuthToken();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(csvFile.getBytes()) {
            @Override
            public String getFilename() {
                return csvFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/upload/" + userIndexId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        assertThat(responseBody.get("statusCode")).isEqualTo(200);
        assertThat(responseBody.get("statusMessage")).isEqualTo("CSV file loaded successfully.");

        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        assertNotNull(data);
        assertThat(data.size()).isEqualTo(3);

        List<Receipt> savedReceipts = receiptRepository.findAllByStudentClub(studentClub);
        assertThat(savedReceipts.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("CSV 다운로드 성공 통합테스트")
    void downloadCsvTest() throws Exception {
        // Given
        Receipt receipt1 = Receipt.builder()
                .date(java.sql.Date.valueOf("2024-01-15"))
                .content("카페 결제")
                .deposit(0)
                .withdrawal(5000)
                .studentClub(studentClub)
                .build();

        Receipt receipt2 = Receipt.builder()
                .date(java.sql.Date.valueOf("2024-01-16"))
                .content("입금")
                .deposit(10000)
                .withdrawal(0)
                .studentClub(studentClub)
                .build();

        receiptRepository.saveAndFlush(receipt1);
        receiptRepository.saveAndFlush(receipt2);

        String authToken = getAuthToken();

        CsvExportDto csvExportDto = CsvExportDto.builder()
                .userId("testUser")
                .year(2024)
                .month(1)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);

        HttpEntity<CsvExportDto> requestEntity = new HttpEntity<>(csvExportDto, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/export",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        String csvContent = response.getBody();
        assertTrue(csvContent.contains("\"date\",\"content\",\"deposit\",\"withdrawal\"") ||
                csvContent.contains("date,content,deposit,withdrawal"));
        assertTrue(csvContent.contains("카페 결제"));
        assertTrue(csvContent.contains("입금"));
        assertTrue(csvContent.contains("5000"));
        assertTrue(csvContent.contains("10000"));
    }

    @Test
    @DisplayName("잘못된 형식의 CSV 파일 업로드 테스트")
    void uploadInvalidCsvTest() throws Exception {
        // Given
        long userIndexId = user.getId();
        String authToken = getAuthToken();

        String invalidCsvContent = "date,content,deposit,withdrawal\n" +
                "invalid-date,카페 결제,invalid-number,5000\n" +
                "2024-01-16,,10000,abc";

        MockMultipartFile invalidCsvFile = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidCsvContent.getBytes()
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(invalidCsvFile.getBytes()) {
            @Override
            public String getFilename() {
                return invalidCsvFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/upload/" + userIndexId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        assertThat(responseBody.get("statusCode")).isEqualTo(200);
        assertThat(responseBody.get("statusMessage")).isEqualTo("CSV file loaded successfully.");

        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        assertNotNull(data);
        assertThat(data.size()).isEqualTo(0);

        List<Receipt> savedReceipts = receiptRepository.findAllByStudentClub(studentClub);
        assertThat(savedReceipts.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("빈 파일 업로드 실패 테스트")
    void uploadEmptyFileTest() throws Exception {
        // Given
        long userIndexId = user.getId();
        String authToken = getAuthToken();

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
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
                "/api/csv/upload/" + userIndexId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        assertThat(data.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("다른 학생회 소속 사용자의 CSV 업로드 실패 테스트")
    void uploadCsvDifferentClubTest() throws Exception {
        // Given
        long userIndexId = user.getId();
        String anotherAuthToken = getAuthToken("anotherUser", "password123!");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(csvFile.getBytes()) {
            @Override
            public String getFilename() {
                return csvFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(anotherAuthToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/upload/" + userIndexId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("다른 학생회 소속 사용자의 CSV 다운로드 실패 테스트")
    void downloadCsvDifferentClubTest() throws Exception {
        // Given
        String anotherAuthToken = getAuthToken("anotherUser", "password123!");

        CsvExportDto csvExportDto = CsvExportDto.builder()
                .userId("testUser")
                .year(2024)
                .month(1)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(anotherAuthToken);

        HttpEntity<CsvExportDto> requestEntity = new HttpEntity<>(csvExportDto, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/export",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("중복 데이터 처리 확인 테스트")
    void uploadCsvDuplicateDataTest() throws Exception {
        // Given
        long userIndexId = user.getId();
        String authToken = getAuthToken();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(csvFile.getBytes()) {
            @Override
            public String getFilename() {
                return csvFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(authToken);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                "/api/csv/upload/" + userIndexId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/upload/" + userIndexId,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        assertThat(data.size()).isEqualTo(0);

        List<Receipt> savedReceipts = receiptRepository.findAllByStudentClub(studentClub);
        assertThat(savedReceipts.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 CSV 다운로드 실패 테스트")
    void downloadCsvInvalidUserIdTest() throws Exception {
        // Given
        String authToken = getAuthToken();

        CsvExportDto csvExportDto = CsvExportDto.builder()
                .userId("nonExistentUser")
                .year(2024)
                .month(1)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);

        HttpEntity<CsvExportDto> requestEntity = new HttpEntity<>(csvExportDto, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/csv/export",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}