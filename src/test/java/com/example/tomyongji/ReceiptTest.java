package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_COLLEGE;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
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
import java.util.Date;
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

    private User user;

    @BeforeEach
    void setup() {
        StudentClub studentClub = studentClubRepository.findById(26L).orElseThrow(()-> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        //User 저장: 융합소프트웨어학부 학생회장
        user = User.builder()
            .userId("testUser")
            .name("test name")
            .studentNum("60000000")
            .collegeName("인공지능소프트웨어융합대학")
            .email("test@example.com")
            .password("password123")
            .role("PRESIDENT")
            .studentClub(studentClub) //저장된 StudentClub 설정
            .build();
        userRepository.saveAndFlush(user);

        //테스트
        System.out.println("유저 ID: " + user.getId());
        userRepository.findAll().forEach(u -> System.out.println("저장된 유저: " + u));
        studentClubRepository.findAll().forEach(sc -> System.out.println("저장된 학생회: " + sc));
    }

    @AfterEach
    void reset() {
        userRepository.delete(user);
        receiptRepository.deleteAll();
    }

    @Test
    @DisplayName("영수증 작성 흐름 테스트")
    void testSaveReceiptFlow() {
        //Given
        String userId = user.getUserId();
        ReceiptCreateDto receiptCreateDto = ReceiptCreateDto.builder()
            .userId(userId)
            .date(new Date())
            .content("테스트")
            .deposit(1000)
            .build();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
        Long clubId = user.getStudentClub().getId();

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", clubId);

        ResponseEntity<ApiResponse<ReceiptByStudentClubDto>> response = restTemplate.exchange(
            "/api/receipt/club/{clubId}",
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
    @DisplayName("특정 영수증 조회 흐름 테스트")
    void testGetReceiptById() throws Exception {
        //Given
        Receipt receipt = Receipt.builder()
            .id(1L) // ID 설정
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

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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

//    @Test
//    @DisplayName("영수증 수정 흐름 테스트")
//    void testUpdateReceiptFlow() {
//        //Given
//        Receipt receipt = Receipt.builder()
//            .content("영수증 테스트")
//            .deposit(1000)
//            .studentClub(user.getStudentClub())
//            .build();
//        receiptRepository.save(receipt);
//        ReceiptDto receiptCreateDto = ReceiptDto.builder()
//            .receiptId(receipt.getId())
//            .date(new Date())
//            .content("수정된 내용")
//            .deposit(2000)
//            .build();
//
//        //When, Then
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<ReceiptDto> entity = new HttpEntity<>(receiptCreateDto, headers);
//
//        ResponseEntity<ApiResponse<ReceiptDto>> response = restTemplate.exchange(
//            "/api/receipt",
//            HttpMethod.PATCH,
//            entity,
//            new ParameterizedTypeReference<ApiResponse<ReceiptDto>>() {}
//        );
//        //Then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//        ApiResponse<ReceiptDto> body = response.getBody();
//        assertNotNull(body);
//        assertThat(body.getStatusCode()).isEqualTo(201);
//        assertThat(body.getStatusMessage()).isEqualTo("영수증을 성공적으로 수정했습니다.");
//        assertThat(body.getData().getContent()).isEqualTo(receiptCreateDto.getContent());
//    }

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
        Long collegeId = 16L; //인공지능소프트웨어융합대학

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

