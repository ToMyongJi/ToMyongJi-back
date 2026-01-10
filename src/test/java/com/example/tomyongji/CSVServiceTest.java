package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.CsvExportDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.service.CSVService;
import com.example.tomyongji.receipt.service.ReceiptService;
import com.example.tomyongji.validation.CustomException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class CSVServiceTest {

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
        studentClub = StudentClub.builder()
                .id(30L)
                .studentClubName("스마트시스템공과대학 학생회")
                .Balance(100000)
                .build();

        anotherStudentClub = StudentClub.builder()
                .id(35L)
                .studentClubName("아너칼리지(자연)")
                .Balance(50000)
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

        String invalidCsvContent = "date,content,deposit,withdrawal\n" +
                "invalid-date,카페 결제,invalid-number,5000\n" +
                "2024-01-16,,10000,0";

        invalidCsvFile = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidCsvContent.getBytes()
        );
    }

    @Test
    @DisplayName("CSV 파일 업로드 성공 테스트")
    void loadDataFromCSV_Success() {
        // Given
        long userIndexId = user.getId();
        when(userRepository.findById(userIndexId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(receiptRepository.existsByDateAndContent(any(Date.class), anyString())).thenReturn(false);

        // When
        List<Receipt> result = csvService.loadDataFromCSV(csvFile, userIndexId, currentUser);
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        Receipt firstReceipt = result.get(0);
        assertEquals("카페 결제", firstReceipt.getContent());
        assertEquals(0, firstReceipt.getDeposit());
        assertEquals(5000, firstReceipt.getWithdrawal());
        assertEquals(studentClub, firstReceipt.getStudentClub());

        Receipt secondReceipt = result.get(1);
        assertEquals("입금", secondReceipt.getContent());
        assertEquals(10000, secondReceipt.getDeposit());
        assertEquals(0, secondReceipt.getWithdrawal());

        verify(receiptRepository, times(3)).save(any(Receipt.class));
        verify(receiptRepository, times(3)).existsByDateAndContent(any(Date.class), anyString());
        verify(receiptService).clearReceiptCache(studentClub.getId());
    }

    @Test
    @DisplayName("존재하지 않는 유저로 CSV 업로드 실패")
    void loadDataFromCSV_NotFoundUser() {
        // Given
        long invalidUserIndexId = 999L;
        when(userRepository.findById(invalidUserIndexId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
                () -> csvService.loadDataFromCSV(csvFile, invalidUserIndexId, currentUser));

        verify(userRepository).findById(invalidUserIndexId);
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("다른 소속 유저의 권한 없음으로 CSV 업로드 실패")
    void loadDataFromCSV_NoAuthorizationBelonging() {
        // Given
        long userIndexId = user.getId();
        when(userRepository.findById(userIndexId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> csvService.loadDataFromCSV(csvFile, userIndexId, anotherCurrentUser));

        assertThat(exception.getErrorCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo(NO_AUTHORIZATION_BELONGING);
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("중복 데이터 처리 테스트")
    void loadDataFromCSV_DuplicateData() {
        // Given
        long userIndexId = user.getId();
        when(userRepository.findById(userIndexId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));

        when(receiptRepository.existsByDateAndContent(any(Date.class), eq("카페 결제"))).thenReturn(true);
        when(receiptRepository.existsByDateAndContent(any(Date.class), eq("입금"))).thenReturn(false);
        when(receiptRepository.existsByDateAndContent(any(Date.class), eq("문구점 결제"))).thenReturn(false);

        // When
        List<Receipt> result = csvService.loadDataFromCSV(csvFile, userIndexId, currentUser);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(receiptRepository, times(2)).save(any(Receipt.class)); // 중복 제외하고 2번만 저장
        verify(receiptRepository, times(3)).existsByDateAndContent(any(Date.class), anyString());
    }

    @Test
    @DisplayName("잘못된 데이터 형식 처리 테스트")
    void loadDataFromCSV_InvalidData() {
        // Given
        long userIndexId = user.getId();
        when(userRepository.findById(userIndexId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));

        // When
        List<Receipt> result = csvService.loadDataFromCSV(invalidCsvFile, userIndexId, currentUser);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("CSV 다운로드 성공 테스트")
    void writeCsv_Success() throws IOException, ParseException {
        // Given
        CsvExportDto csvExportDto = CsvExportDto.builder()
                .userId("testUser")
                .year(2024)
                .month(1)
                .build();

        Receipt receipt1 = Receipt.builder()
                .date(new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-15"))
                .content("카페 결제")
                .deposit(0)
                .withdrawal(5000)
                .studentClub(studentClub)
                .build();

        Receipt receipt2 = Receipt.builder()
                .date(new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-16"))
                .content("입금")
                .deposit(10000)
                .withdrawal(0)
                .studentClub(studentClub)
                .build();

        List<Receipt> receipts = Arrays.asList(receipt1, receipt2);

        when(userRepository.findByUserId("testUser")).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(receiptRepository.findByStudentClubAndDateBetween(eq(studentClub), any(Date.class), any(Date.class)))
                .thenReturn(receipts);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        csvService.writeCsv(response, csvExportDto, currentUser);

        // Then
        String csvOutput = stringWriter.toString();
        assertThat(csvOutput).contains("\"date\",\"content\",\"deposit\",\"withdrawal\"");
        assertThat(csvOutput).contains("\"2024-01-15\",\"카페 결제\",\"0\",\"5000\"");
        assertThat(csvOutput).contains("\"2024-01-16\",\"입금\",\"10000\",\"0\"");

        verify(response).setContentType("text/csv");
        verify(response).setHeader(eq("Content-Disposition"), anyString());
        verify(receiptRepository).findByStudentClubAndDateBetween(eq(studentClub), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 CSV 다운로드 실패")
    void writeCsv_NotFoundUser() {
        // Given
        CsvExportDto csvExportDto = CsvExportDto.builder()
                .userId("nonExistentUser")
                .year(2024)
                .month(1)
                .build();

        when(userRepository.findByUserId("nonExistentUser")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
                () -> csvService.writeCsv(response, csvExportDto, currentUser));

        verify(userRepository).findByUserId("nonExistentUser");
        verify(receiptRepository, never()).findByStudentClubAndDateBetween(any(), any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("다른 소속 유저의 권한 없음으로 CSV 다운로드 실패")
    void writeCsv_NoAuthorizationBelonging() {
        // Given
        CsvExportDto csvExportDto = CsvExportDto.builder()
                .userId("testUser")
                .year(2024)
                .month(1)
                .build();

        when(userRepository.findByUserId("testUser")).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> csvService.writeCsv(response, csvExportDto, anotherCurrentUser));

        assertThat(exception.getErrorCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo(NO_AUTHORIZATION_BELONGING);
        verify(receiptRepository, never()).findByStudentClubAndDateBetween(any(), any(Date.class), any(Date.class));
    }
}