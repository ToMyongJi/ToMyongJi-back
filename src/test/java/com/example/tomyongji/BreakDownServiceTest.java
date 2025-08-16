package com.example.tomyongji;

import java.io.IOException;

import static com.example.tomyongji.validation.ErrorMsg.EMPTY_FILE;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.receipt.service.BreakDownService;
import com.example.tomyongji.validation.CustomException;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class BreakDownServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private StudentClubRepository studentClubRepository;

    @Mock
    private ReceiptMapper mapper;

    @InjectMocks
    private BreakDownService breakDownService;

    private User user;
    private User anotherUser;
    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private MockMultipartFile pdfFile;
    private MockMultipartFile emptyPdfFile;
    private UserDetails currentUser;
    private UserDetails anotherCurrentUser;

    @BeforeEach
    void setUp() throws IOException {
        studentClub = StudentClub.builder()
                .id(30L)
                .studentClubName("스마트시스템공과대학 학생회")
                .build();

        anotherStudentClub = StudentClub.builder()
                .id(35L)
                .studentClubName("아너칼리지(자연)")
                .build();

        user = User.builder()
                .id(1L)
                .userId("testUser")
                .name("테스트유저")
                .studentNum("60221317")
                .studentClub(studentClub)
                .collegeName("스마트시스템공과대학")
                .email("test@example.com")
                .password("password123!")
                .role("PRESIDENT")
                .build();

        anotherUser = User.builder()
                .id(2L)
                .userId("anotherUser")
                .name("다른유저")
                .studentNum("60000001")
                .studentClub(anotherStudentClub)
                .collegeName("아너칼리지")
                .email("another@example.com")
                .password("password123!")
                .role("PRESIDENT")
                .build();

        currentUser = org.springframework.security.core.userdetails.User.builder()
                .username("testUser")
                .password("password123!")
                .authorities("ROLE_PRESIDENT")
                .build();

        anotherCurrentUser = org.springframework.security.core.userdetails.User.builder()
                .username("anotherUser")
                .password("password123!")
                .authorities("ROLE_PRESIDENT")
                .build();


        String[] possibleFileNames = {
                "breakdown_test.pdf",
                "test.pdf",
                "sample.pdf"
        };

        ClassPathResource resource = null;
        String foundFileName = null;

        for (String fileName : possibleFileNames) {
            resource = new ClassPathResource(fileName);
            if (resource.exists()) {
                foundFileName = fileName;
                break;
            }
        }

        try {
            byte[] pdfBytes = StreamUtils.copyToByteArray(resource.getInputStream());
            System.out.println("PDF 파일 로드 성공: " + foundFileName + ", 크기: " + pdfBytes.length + " bytes");

            pdfFile = new MockMultipartFile(
                    "file",
                    foundFileName,
                    "application/pdf",
                    pdfBytes
            );
        } catch (IOException e) {
            throw new RuntimeException("PDF 파일 로드 실패: " + e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("PDF 파싱 성공 테스트")
    void parsePdf_Success() throws Exception {
        // Given
        String userId = user.getUserId();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));

        // When
        BreakDownDto result = breakDownService.parsePdf(pdfFile, userId, currentUser);

        // Then
        assertNotNull(result);
        assertThat(result.getIssueDate()).isNotNull();
        assertThat(result.getIssueNumber()).isNotNull();
        assertThat(result.getStudentClubId()).isEqualTo(studentClub.getId());

        verify(userRepository, times(2)).findByUserId(userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저로 PDF 파싱 실패")
    void parsePdf_NotFoundUser() {
        // Given
        String invalidUserId = "nonExistentUser";
        when(userRepository.findByUserId(invalidUserId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> breakDownService.parsePdf(pdfFile, invalidUserId, currentUser));

        assertThat(exception.getErrorCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_USER);
        verify(userRepository).findByUserId(invalidUserId);
    }

    @Test
    @DisplayName("학생회가 없는 유저로 PDF 파싱 실패")
    void parsePdf_NotFoundStudentClub() {
        // Given
        User userWithoutClub = User.builder()
                .id(1L)
                .userId("testUser")
                .studentClub(null)
                .build();

        when(userRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(userWithoutClub));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(userWithoutClub));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> breakDownService.parsePdf(pdfFile, user.getUserId(), currentUser));

        assertThat(exception.getErrorCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_STUDENT_CLUB);
    }

    @Test
    @DisplayName("빈 PDF 파일로 파싱 실패")
    void parsePdf_EmptyFile() {
        // Given
        when(userRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> breakDownService.parsePdf(emptyPdfFile, user.getUserId(), currentUser));

        assertThat(exception.getErrorCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo(EMPTY_FILE);
    }

    @Test
    @DisplayName("다른 소속 유저의 권한 없음으로 PDF 파싱 실패")
    void parsePdf_NoAuthorizationBelonging() {
        // Given
        when(userRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> breakDownService.parsePdf(pdfFile, user.getUserId(), anotherCurrentUser));

        assertThat(exception.getErrorCode()).isEqualTo(403);
        assertThat(exception.getMessage()).isEqualTo(NO_AUTHORIZATION_BELONGING);
    }

    @Test
    @DisplayName("외부 API 정상 응답 처리 성공")
    void fetchAndProcessDocument_Success() throws ParseException {
        // Given
        BreakDownDto dto = BreakDownDto.builder()
                .issueDate("2024-01-15")
                .issueNumber("ABC123")
                .studentClubId(studentClub.getId())
                .build();

        String mockHtmlResponse = """
                <table class="table">
                    <tbody>
                        <tr>
                            <td>2024-01-15 10:30:00</td>
                            <td></td>
                            <td>-5000</td>
                            <td></td>
                            <td>카페 결제</td>
                        </tr>
                        <tr>
                            <td>2024-01-15 14:20:00</td>
                            <td></td>
                            <td>10000</td>
                            <td></td>
                            <td>입금</td>
                        </tr>
                    </tbody>
                </table>
                """;

        ResponseEntity<String> mockResponse = new ResponseEntity<>(mockHtmlResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(String.class), any(), any()))
                .thenReturn(mockResponse);
        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));

        Receipt receipt1 = Receipt.builder()
                .content("카페 결제")
                .withdrawal(5000)
                .studentClub(studentClub)
                .verification(true)
                .build();
        Receipt receipt2 = Receipt.builder()
                .content("입금")
                .deposit(10000)
                .studentClub(studentClub)
                .verification(true)
                .build();

        ReceiptDto receiptDto1 = ReceiptDto.builder()
                .content("카페 결제")
                .withdrawal(5000)
                .build();
        ReceiptDto receiptDto2 = ReceiptDto.builder()
                .content("입금")
                .deposit(10000)
                .build();

        when(mapper.toReceiptDto(any(Receipt.class)))
                .thenReturn(receiptDto1)
                .thenReturn(receiptDto2);

        // When
        List<ReceiptDto> result = breakDownService.fetchAndProcessDocument(dto);

        // Then
        assertNotNull(result);
        assertThat(result.size()).isEqualTo(2);

        verify(restTemplate).getForEntity(anyString(), eq(String.class), any(), any());
        verify(receiptRepository).saveAll(any());
        verify(mapper, times(2)).toReceiptDto(any(Receipt.class));

        verify(restTemplate).getForEntity(
                contains("api.tossbank.com"),
                eq(String.class),
                eq("2024-01-15"),
                eq("ABC123")
        );
        verify(receiptRepository).saveAll(any());
        verify(mapper, times(2)).toReceiptDto(any(Receipt.class));
    }

    @Test
    @DisplayName("PDF 파일 유효성 검사 - 정상 파일")
    void validatePdfFile_ValidFile() {
        // When
        boolean result = breakDownService.validatePdfFile(pdfFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("PDF 파일 유효성 검사 - 빈 파일")
    void validatePdfFile_EmptyFile() {
        // When
        boolean result = breakDownService.validatePdfFile(emptyPdfFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("PDF 파일 유효성 검사 - null 파일")
    void validatePdfFile_NullFile() {
        // When
        boolean result = breakDownService.validatePdfFile(null);

        // Then
        assertThat(result).isFalse();
    }
}