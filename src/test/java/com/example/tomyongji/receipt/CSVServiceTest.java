package com.example.tomyongji.receipt;

import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.CsvExportDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.domain.receipt.service.CSVService;
import com.example.tomyongji.domain.receipt.service.ReceiptService;
import com.example.tomyongji.global.error.CustomException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CSVServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ReceiptService receiptService;

    @InjectMocks
    private CSVService csvService;

    private User user;
    private User anotherUser;
    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private UserDetails currentUser;
    private UserDetails anotherCurrentUser;
    private MockMultipartFile csvFile;
    private MockMultipartFile invalidCsvFile;

    @BeforeEach
    void setUp() {
        studentClub = createStudentClub(30L, "스마트시스템공과대학 학생회", 100000);
        anotherStudentClub = createStudentClub(35L, "아너칼리지(자연)", 50000);

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

        csvFile = createValidCsvFile();
        invalidCsvFile = createInvalidCsvFile();
    }

    private StudentClub createStudentClub(Long id, String name, Integer balance) {
        return StudentClub.builder()
                .id(id)
                .studentClubName(name)
                .Balance(balance)
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

    private MockMultipartFile createValidCsvFile() {
        String csvContent = "date,content,deposit,withdrawal\n" +
                "2024-01-15,카페 결제,0,5000\n" +
                "2024-01-16,입금,10000,0\n" +
                "2024-01-17,문구점 결제,0,3000";

        return new MockMultipartFile(
                "file",
                "receipts.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile createInvalidCsvFile() {
        String invalidCsvContent = "date,content,deposit,withdrawal\n" +
                "invalid-date,카페 결제,invalid-number,5000\n" +
                "2024-01-16,,10000,0";

        return new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidCsvContent.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Receipt createReceipt(Date date, String content, int deposit, int withdrawal) {
        return Receipt.builder()
                .date(date)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .studentClub(studentClub)
                .build();
    }

    @Nested
    @DisplayName("loadDataFromCSV 메서드는")
    class Describe_loadDataFromCSV {

        @Nested
        @DisplayName("유효한 CSV 파일이 주어지면")
        class Context_with_valid_csv {

            @Test
            @DisplayName("모든 영수증을 성공적으로 저장한다")
            void it_saves_all_receipts_successfully() {
                // given
                long userIndexId = user.getId();
                given(userRepository.findById(userIndexId)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptRepository.existsByDateAndContent(any(Date.class), anyString())).willReturn(false);

                // when
                List<Receipt> result = csvService.loadDataFromCSV(csvFile, userIndexId, currentUser);

                // then
                assertThat(result).isNotNull()
                        .hasSize(3);

                assertThat(result.get(0))
                        .extracting(
                                Receipt::getContent,
                                Receipt::getDeposit,
                                Receipt::getWithdrawal,
                                Receipt::getStudentClub
                        )
                        .containsExactly(
                                "카페 결제",
                                0,
                                5000,
                                studentClub
                        );

                assertThat(result.get(1))
                        .extracting(
                                Receipt::getContent,
                                Receipt::getDeposit,
                                Receipt::getWithdrawal
                        )
                        .containsExactly(
                                "입금",
                                10000,
                                0
                        );

                then(receiptRepository).should(times(3)).save(any(Receipt.class));
                then(receiptRepository).should(times(3)).existsByDateAndContent(any(Date.class), anyString());
                then(receiptService).should().clearReceiptCache(studentClub.getId());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저 ID가 주어지면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("RuntimeException을 던진다")
            void it_throws_runtime_exception() {
                // given
                long invalidUserIndexId = 999L;
                given(userRepository.findById(invalidUserIndexId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> csvService.loadDataFromCSV(csvFile, invalidUserIndexId, currentUser))
                        .isInstanceOf(RuntimeException.class);

                then(userRepository).should().findById(invalidUserIndexId);
                then(receiptRepository).should(never()).save(any(Receipt.class));
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                long userIndexId = user.getId();
                given(userRepository.findById(userIndexId)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> csvService.loadDataFromCSV(csvFile, userIndexId, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);

                then(receiptRepository).should(never()).save(any(Receipt.class));
            }
        }

        @Nested
        @DisplayName("중복 데이터가 포함된 CSV가 주어지면")
        class Context_with_duplicate_data {

            @Test
            @DisplayName("중복되지 않은 데이터만 저장한다")
            void it_saves_only_non_duplicate_data() {
                // given
                long userIndexId = user.getId();
                given(userRepository.findById(userIndexId)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                given(receiptRepository.existsByDateAndContent(any(Date.class), eq("카페 결제"))).willReturn(true);
                given(receiptRepository.existsByDateAndContent(any(Date.class), eq("입금"))).willReturn(false);
                given(receiptRepository.existsByDateAndContent(any(Date.class), eq("문구점 결제"))).willReturn(false);

                // when
                List<Receipt> result = csvService.loadDataFromCSV(csvFile, userIndexId, currentUser);

                // then
                assertThat(result).isNotNull()
                        .hasSize(2);

                then(receiptRepository).should(times(2)).save(any(Receipt.class)); // 중복 제외하고 2번만 저장
                then(receiptRepository).should(times(3)).existsByDateAndContent(any(Date.class), anyString());
            }
        }

        @Nested
        @DisplayName("잘못된 형식의 CSV가 주어지면")
        class Context_with_invalid_format {

            @Test
            @DisplayName("빈 리스트를 반환한다")
            void it_returns_empty_list() {
                // given
                long userIndexId = user.getId();
                given(userRepository.findById(userIndexId)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when
                List<Receipt> result = csvService.loadDataFromCSV(invalidCsvFile, userIndexId, currentUser);

                // then
                assertThat(result).isNotNull()
                        .isEmpty();

                then(receiptRepository).should(never()).save(any(Receipt.class));
            }
        }
    }

    @Nested
    @DisplayName("writeCsv 메서드는")
    class Describe_writeCsv {

        @Nested
        @DisplayName("유효한 CSV 다운로드 요청이 주어지면")
        class Context_with_valid_export_request {

            @Test
            @DisplayName("CSV 파일을 성공적으로 생성한다")
            void it_generates_csv_successfully() throws IOException, ParseException {
                // given
                CsvExportDto csvExportDto = CsvExportDto.builder()
                        .userId("testUser")
                        .year(2024)
                        .month(1)
                        .build();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Receipt receipt1 = createReceipt(
                        dateFormat.parse("2024-01-15"),
                        "카페 결제",
                        0,
                        5000
                );
                Receipt receipt2 = createReceipt(
                        dateFormat.parse("2024-01-16"),
                        "입금",
                        10000,
                        0
                );

                List<Receipt> receipts = Arrays.asList(receipt1, receipt2);

                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptRepository.findByStudentClubAndDateBetween(eq(studentClub), any(Date.class), any(Date.class)))
                        .willReturn(receipts);

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                given(response.getWriter()).willReturn(printWriter);

                // when
                csvService.writeCsv(response, csvExportDto, currentUser);

                // then
                String csvOutput = stringWriter.toString();
                assertThat(csvOutput)
                        .contains("\"date\",\"content\",\"deposit\",\"withdrawal\"")
                        .contains("\"2024-01-15\",\"카페 결제\",\"0\",\"5000\"")
                        .contains("\"2024-01-16\",\"입금\",\"10000\",\"0\"");

                then(response).should().setContentType("text/csv");
                then(response).should().setHeader(eq("Content-Disposition"), anyString());
                then(receiptRepository).should().findByStudentClubAndDateBetween(
                        eq(studentClub),
                        any(Date.class),
                        any(Date.class)
                );
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저 ID가 주어지면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("RuntimeException을 던진다")
            void it_throws_runtime_exception() {
                // given
                CsvExportDto csvExportDto = CsvExportDto.builder()
                        .userId("nonExistentUser")
                        .year(2024)
                        .month(1)
                        .build();

                given(userRepository.findByUserId("nonExistentUser")).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> csvService.writeCsv(response, csvExportDto, currentUser))
                        .isInstanceOf(RuntimeException.class);

                then(userRepository).should().findByUserId("nonExistentUser");
                then(receiptRepository).should(never()).findByStudentClubAndDateBetween(
                        any(),
                        any(Date.class),
                        any(Date.class)
                );
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                CsvExportDto csvExportDto = CsvExportDto.builder()
                        .userId("testUser")
                        .year(2024)
                        .month(1)
                        .build();

                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> csvService.writeCsv(response, csvExportDto, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);

                then(receiptRepository).should(never()).findByStudentClubAndDateBetween(
                        any(),
                        any(Date.class),
                        any(Date.class)
                );
            }
        }
    }
}