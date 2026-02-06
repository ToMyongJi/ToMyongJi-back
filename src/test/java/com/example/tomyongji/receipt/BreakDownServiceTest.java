package com.example.tomyongji.receipt;

import java.io.IOException;

import static com.example.tomyongji.global.error.ErrorMsg.EMPTY_FILE;
import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.BreakDownDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.service.BreakDownService;
import com.example.tomyongji.domain.receipt.service.ReceiptService;
import com.example.tomyongji.global.error.CustomException;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class BreakDownServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private StudentClubRepository studentClubRepository;

    @Mock
    private ReceiptMapper mapper;

    @Mock
    private ReceiptService receiptService;

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
        studentClub = createStudentClub(30L, "스마트시스템공과대학 학생회");
        anotherStudentClub = createStudentClub(35L, "아너칼리지(자연)");

        user = createUser(
                1L,
                "testUser",
                "테스트유저",
                "60221317",
                studentClub,
                "스마트시스템공과대학",
                "test@example.com",
                "password123!",
                "PRESIDENT"
        );

        anotherUser = createUser(
                2L,
                "anotherUser",
                "다른유저",
                "60000001",
                anotherStudentClub,
                "아너칼리지",
                "another@example.com",
                "password123!",
                "PRESIDENT"
        );

        currentUser = createUserDetails("testUser", "password123!");
        anotherCurrentUser = createUserDetails("anotherUser", "password123!");

        pdfFile = loadPdfFile();
        emptyPdfFile = null; // 빈 파일은 null로 처리
    }

    private StudentClub createStudentClub(Long id, String name) {
        return StudentClub.builder()
                .id(id)
                .studentClubName(name)
                .build();
    }

    private User createUser(Long id, String userId, String name, String studentNum,
                            StudentClub studentClub, String collegeName, String email,
                            String password, String role) {
        return User.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .studentNum(studentNum)
                .studentClub(studentClub)
                .collegeName(collegeName)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }

    private UserDetails createUserDetails(String username, String password) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(password)
                .authorities("ROLE_PRESIDENT")
                .build();
    }

    private MockMultipartFile loadPdfFile() throws IOException {
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

        byte[] pdfBytes = StreamUtils.copyToByteArray(resource.getInputStream());
        return new MockMultipartFile(
                "file",
                foundFileName,
                "application/pdf",
                pdfBytes
        );
    }

    private void mockRestClientCall(String mockHtmlResponse) {
        given(restClient.get()).willReturn(requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(anyString(), any(), any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(String.class)).willReturn(mockHtmlResponse);
    }

    private ReceiptRepository.ReceiptCount createReceiptCount(Long total, Long verified) {
        return new ReceiptRepository.ReceiptCount() {
            @Override
            public Long getTotal() { return total; }
            @Override
            public Long getVerified() { return verified; }
        };
    }

    @Nested
    @DisplayName("parsePdf 메서드는")
    class Describe_parsePdf {

        @Nested
        @DisplayName("유효한 PDF 파일과 유저 정보가 주어지면")
        class Context_with_valid_pdf {

            @Test
            @DisplayName("PDF를 성공적으로 파싱한다")
            void it_parses_successfully() throws Exception {
                // given
                String userId = user.getUserId();
                String keyword = "학생회비";

                given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when
                BreakDownDto result = breakDownService.parsePdf(pdfFile, userId, keyword, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getIssueDate()).isNotNull();
                assertThat(result.getIssueNumber()).isNotNull();
                assertThat(result.getStudentClubId()).isEqualTo(studentClub.getId());

                then(userRepository).should(times(2)).findByUserId(userId);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저 ID가 주어지면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // given
                String invalidUserId = "nonExistentUser";
                String keyword = "학생회비";
                given(userRepository.findByUserId(invalidUserId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> breakDownService.parsePdf(pdfFile, invalidUserId, keyword, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);

                then(userRepository).should().findByUserId(invalidUserId);
            }
        }

        @Nested
        @DisplayName("학생회가 없는 유저가 주어지면")
        class Context_with_student_club_not_found {

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // given
                String keyword = "학생회비";
                User userWithoutClub = User.builder()
                        .id(1L)
                        .userId("testUser")
                        .studentClub(null)
                        .build();

                given(userRepository.findByUserId(user.getUserId())).willReturn(Optional.of(userWithoutClub));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(userWithoutClub));

                // when & then
                assertThatThrownBy(() -> breakDownService.parsePdf(pdfFile, user.getUserId(), keyword, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_STUDENT_CLUB);
            }
        }

        @Nested
        @DisplayName("빈 PDF 파일이 주어지면")
        class Context_with_empty_file {

            @Test
            @DisplayName("EMPTY_FILE 예외를 던진다")
            void it_throws_empty_file_exception() {
                // given
                String keyword = "학생회비";
                given(userRepository.findByUserId(user.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> breakDownService.parsePdf(emptyPdfFile, user.getUserId(), keyword, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", EMPTY_FILE);
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                String keyword = "테스트키워드";
                given(userRepository.findByUserId(user.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> breakDownService.parsePdf(pdfFile, user.getUserId(), keyword, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 403)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);
            }
        }
    }

    @Nested
    @DisplayName("fetchAndProcessDocument 메서드는")
    class Describe_fetchAndProcessDocument {

        @Nested
        @DisplayName("외부 API가 정상 응답하면")
        class Context_with_valid_response {

            @Test
            @DisplayName("영수증을 성공적으로 저장하고 반환한다")
            void it_saves_receipts_successfully() throws ParseException {
                // given
                BreakDownDto dto = BreakDownDto.builder()
                        .issueDate("2024-01-15")
                        .issueNumber("ABC123")
                        .studentClubId(studentClub.getId())
                        .keyword(null)
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

                mockRestClientCall(mockHtmlResponse);
                given(studentClubRepository.findById(studentClub.getId())).willReturn(Optional.of(studentClub));
                given(receiptRepository.countTotalAndVerified(studentClub)).willReturn(createReceiptCount(0L, 0L));

                ReceiptDto receiptDto1 = ReceiptDto.builder()
                        .content("카페 결제")
                        .withdrawal(5000)
                        .build();
                ReceiptDto receiptDto2 = ReceiptDto.builder()
                        .content("입금")
                        .deposit(10000)
                        .build();

                given(mapper.toReceiptDto(any(Receipt.class)))
                        .willReturn(receiptDto1)
                        .willReturn(receiptDto2);

                // when
                List<ReceiptDto> result = breakDownService.fetchAndProcessDocument(dto);

                // then
                assertThat(result).isNotNull()
                        .hasSize(2);

                then(restClient).should().get();
                then(requestHeadersUriSpec).should().uri(anyString(), any(), any());
                then(requestHeadersSpec).should().retrieve();
                then(responseSpec).should().body(String.class);
                then(receiptRepository).should().saveAll(any());
                then(studentClubRepository).should().save(studentClub);
                then(mapper).should(times(2)).toReceiptDto(any(Receipt.class));
                then(receiptService).should().clearReceiptCache(studentClub.getId());
                then(receiptService).should().checkAndUpdateVerificationStatus(studentClub.getId(), 2L, 2L);
            }
        }

        @Nested
        @DisplayName("키워드가 포함된 요청이면")
        class Context_with_keyword {

            @Test
            @DisplayName("영수증 내용에 키워드를 접두사로 추가한다")
            void it_adds_keyword_prefix_to_content() throws ParseException {
                // given
                String keyword = "학생회비";
                BreakDownDto dto = BreakDownDto.builder()
                        .issueDate("2024-01-15")
                        .issueNumber("ABC123")
                        .studentClubId(studentClub.getId())
                        .keyword(keyword)
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
                            </tbody>
                        </table>
                        """;

                mockRestClientCall(mockHtmlResponse);
                given(studentClubRepository.findById(studentClub.getId())).willReturn(Optional.of(studentClub));
                given(receiptRepository.countTotalAndVerified(studentClub)).willReturn(createReceiptCount(0L, 0L));

                ReceiptDto receiptDto = ReceiptDto.builder()
                        .content("[학생회비] 카페 결제")
                        .withdrawal(5000)
                        .build();

                given(mapper.toReceiptDto(any(Receipt.class))).willReturn(receiptDto);

                // when
                List<ReceiptDto> result = breakDownService.fetchAndProcessDocument(dto);

                // then
                assertThat(result).isNotNull()
                        .hasSize(1);
                assertThat(result.get(0).getContent()).contains("[" + keyword + "]");

                then(restClient).should().get();
                then(receiptRepository).should().saveAll(any());
                then(studentClubRepository).should().save(studentClub);
                then(receiptService).should().clearReceiptCache(studentClub.getId());
                then(receiptService).should().checkAndUpdateVerificationStatus(studentClub.getId(), 1L, 1L);
            }
        }
    }

    @Nested
    @DisplayName("validatePdfFile 메서드는")
    class Describe_validatePdfFile {

        @Nested
        @DisplayName("유효한 PDF 파일이 주어지면")
        class Context_with_valid_file {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // when
                boolean result = breakDownService.validatePdfFile(pdfFile);

                // then
                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("빈 PDF 파일이 주어지면")
        class Context_with_empty_file {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // when
                boolean result = breakDownService.validatePdfFile(emptyPdfFile);

                // then
                assertThat(result).isFalse();
            }
        }

        @Nested
        @DisplayName("null 파일이 주어지면")
        class Context_with_null_file {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // when
                boolean result = breakDownService.validatePdfFile(null);

                // then
                assertThat(result).isFalse();
            }
        }
    }
}