package com.example.tomyongji.receipt;

import static com.example.tomyongji.global.error.ErrorMsg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelConfirmRequestDto;
import com.example.tomyongji.domain.receipt.dto.ExcelPreviewItemDto;
import com.example.tomyongji.domain.receipt.dto.ExcelStatusResponseDto;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ExcelMappingRuleRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.service.ExcelAnalyzeService;
import com.example.tomyongji.domain.receipt.service.ExcelConfirmService;
import com.example.tomyongji.domain.receipt.service.ReceiptService;
import com.example.tomyongji.global.error.CustomException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ObjectMapper objectMapper;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private MultipartFile multipartFile;
    @Mock private StudentClubRepository studentClubRepository;
    @Mock private ExcelMappingRuleRepository excelMappingRuleRepository;
    @Mock private ReceiptService receiptService;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ExcelAnalyzeService excelAnalyzeService;

    @InjectMocks
    private ExcelConfirmService excelConfirmService;

    private StudentClub studentClub;
    private StudentClub anotherClub;
    private User user;
    private User anotherUser;
    private UserDetails currentUser;
    private UserDetails anotherCurrentUser;

    @BeforeEach
    void setUp() {
        studentClub = StudentClub.builder().id(1L).studentClubName("테스트학생회").Balance(100000).build();
        anotherClub = StudentClub.builder().id(2L).studentClubName("다른학생회").Balance(50000).build();

        user = User.builder()
            .id(1L).userId("testUser").name("테스트유저")
            .studentNum("60000001").email("test@example.com")
            .password("pw").role("PRESIDENT")
            .studentClub(studentClub)
            .build();

        anotherUser = User.builder()
            .id(2L).userId("anotherUser").name("다른유저")
            .studentNum("60000002").email("another@example.com")
            .password("pw").role("PRESIDENT")
            .studentClub(anotherClub)
            .build();

        currentUser = new org.springframework.security.core.userdetails.User(
            "testUser", "pw", Collections.emptyList());
        anotherCurrentUser = new org.springframework.security.core.userdetails.User(
            "anotherUser", "pw", Collections.emptyList());
    }

    @Nested
    @DisplayName("ExcelAnalyzeService.analyze 메서드는")
    class Describe_analyze {

        @Nested
        @DisplayName("존재하지 않는 유저가 요청하면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                given(userRepository.findByUserId("testUser")).willReturn(Optional.empty());

                assertThatThrownBy(() -> excelAnalyzeService.analyze(multipartFile, currentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 400)
                    .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);
            }
        }

        @Nested
        @DisplayName("소속 학생회가 없는 유저가 요청하면")
        class Context_with_no_student_club {

            @Test
            @DisplayName("NOT_HAVE_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_have_student_club_exception() {
                User userWithoutClub = User.builder().id(3L).userId("testUser").studentClub(null).build();
                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(userWithoutClub));

                assertThatThrownBy(() -> excelAnalyzeService.analyze(multipartFile, currentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 401)
                    .hasFieldOrPropertyWithValue("message", NOT_HAVE_STUDENT_CLUB);
            }
        }

        @Nested
        @DisplayName("파일 InputStream 읽기에 실패하면")
        class Context_with_file_read_error {

            @Test
            @DisplayName("EXCEL_PARSE_ERROR 예외를 던진다")
            void it_throws_excel_parse_error() throws IOException {
                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(multipartFile.getInputStream()).willThrow(new IOException("IO error"));

                assertThatThrownBy(() -> excelAnalyzeService.analyze(multipartFile, currentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 400)
                    .hasFieldOrPropertyWithValue("message", EXCEL_PARSE_ERROR);
            }
        }
    }

    @Nested
    @DisplayName("ExcelConfirmService.confirm 메서드는")
    class Describe_confirm {

        private ExcelConfirmRequestDto requestDto;

        @BeforeEach
        void setUpDto() {
            requestDto = new ExcelConfirmRequestDto();
            requestDto.setRequestId("test-request-id");
            requestDto.setStudentClubId(1L);
        }

        @Nested
        @DisplayName("존재하지 않는 유저가 요청하면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                given(userRepository.findByUserId("testUser")).willReturn(Optional.empty());

                assertThatThrownBy(() -> excelConfirmService.confirm(requestDto, currentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 400)
                    .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회 ID가 주어지면")
        class Context_with_student_club_not_found {

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(studentClubRepository.findById(1L)).willReturn(Optional.empty());

                assertThatThrownBy(() -> excelConfirmService.confirm(requestDto, currentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 404)
                    .hasFieldOrPropertyWithValue("message", NOT_FOUND_STUDENT_CLUB);
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                given(userRepository.findByUserId("anotherUser")).willReturn(Optional.of(anotherUser));
                given(studentClubRepository.findById(1L)).willReturn(Optional.of(studentClub));

                assertThatThrownBy(() -> excelConfirmService.confirm(requestDto, anotherCurrentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 400)
                    .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);
            }
        }

        @Nested
        @DisplayName("Redis 미리보기 데이터가 만료되었으면")
        class Context_with_expired_preview {

            @Test
            @DisplayName("EXCEL_PREVIEW_EXPIRED 예외를 던진다")
            void it_throws_excel_preview_expired_exception() {
                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(studentClubRepository.findById(1L)).willReturn(Optional.of(studentClub));
                given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
                given(valueOperations.get("excel:preview:test-request-id")).willReturn(null);

                assertThatThrownBy(() -> excelConfirmService.confirm(requestDto, currentUser))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", 400)
                    .hasFieldOrPropertyWithValue("message", EXCEL_PREVIEW_EXPIRED);
            }
        }

        @Nested
        @DisplayName("유효한 미리보기 데이터가 있으면")
        class Context_with_valid_preview {

            @Test
            @DisplayName("영수증을 bulk insert하고 건수를 반환한다")
            void it_inserts_receipts_and_returns_count() throws Exception {
                List<ExcelPreviewItemDto> previewData = List.of(
                    ExcelPreviewItemDto.builder()
                        .date(new Date()).content("테스트 입금").deposit(5000).withdrawal(0).build(),
                    ExcelPreviewItemDto.builder()
                        .date(new Date()).content("테스트 출금").deposit(0).withdrawal(3000).build()
                );
                String previewJson = "[{\"content\":\"테스트 입금\"},{\"content\":\"테스트 출금\"}]";

                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(studentClubRepository.findById(1L)).willReturn(Optional.of(studentClub));
                given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
                given(valueOperations.get("excel:preview:test-request-id")).willReturn(previewJson);
                given(objectMapper.readValue(anyString(), any(TypeReference.class))).willReturn(previewData);
                given(valueOperations.get("excel:mapping:test-request-id")).willReturn(null);
                given(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                    .willReturn(new int[]{1, 1});

                int result = excelConfirmService.confirm(requestDto, currentUser);

                assertThat(result).isEqualTo(2);
                then(jdbcTemplate).should()
                    .batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
            }
        }
    }
}
