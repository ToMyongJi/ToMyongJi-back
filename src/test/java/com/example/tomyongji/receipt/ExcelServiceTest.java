package com.example.tomyongji.receipt;

import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelExportDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.domain.receipt.service.ExcelService;
import com.example.tomyongji.global.error.CustomException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ExcelServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ExcelService excelService;

    private User user;
    private User anotherUser;
    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private UserDetails currentUser;
    private UserDetails anotherCurrentUser;

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

    private Receipt createReceipt(Date date, String content, int deposit, int withdrawal) {
        return Receipt.builder()
                .date(date)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .studentClub(studentClub)
                .build();
    }

    /**
     * ByteArrayOutputStream을 감싸는 ServletOutputStream 구현체.
     * HttpServletResponse.getOutputStream() mock에서 실제 바이트 내용을 검증하기 위해 사용한다.
     */
    private static class ByteArrayServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer;

        ByteArrayServletOutputStream(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }

        @Override
        public void write(int b) {
            buffer.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        byte[] toByteArray() {
            return buffer.toByteArray();
        }
    }

    @Nested
    @DisplayName("writeExcel 메서드는")
    class Describe_writeExcel {

        @Nested
        @DisplayName("올바른 권한과 유저로 호출하면")
        class Context_with_valid_user_and_permission {

            @Test
            @DisplayName("응답 헤더를 설정하고 엑셀 데이터를 OutputStream에 write한다")
            void it_writes_excel_data_with_headers() throws IOException, ParseException {
                // given
                ExcelExportDto dto = ExcelExportDto.builder()
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
                given(receiptRepository.findByStudentClubAndDateBetween(
                        eq(studentClub), any(Date.class), any(Date.class)))
                        .willReturn(receipts);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ByteArrayServletOutputStream servletOutputStream =
                        new ByteArrayServletOutputStream(byteArrayOutputStream);
                given(response.getOutputStream()).willReturn(servletOutputStream);

                // when
                excelService.writeExcel(response, dto, currentUser);

                // then
                then(response).should().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                then(response).should().setHeader(eq("Content-Disposition"), anyString());
                byte[] writtenBytes = servletOutputStream.toByteArray();
                org.assertj.core.api.Assertions.assertThat(writtenBytes).isNotEmpty();
            }
        }

        @Nested
        @DisplayName("현재 로그인 유저의 학생회와 dto.userId의 학생회가 다른 경우")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING CustomException을 발생시킨다")
            void it_throws_no_authorization_exception() {
                // given
                ExcelExportDto dto = ExcelExportDto.builder()
                        .userId("testUser")
                        .year(2024)
                        .month(1)
                        .build();

                given(userRepository.findByUserId("testUser")).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername()))
                        .willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> excelService.writeExcel(response, dto, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);

                then(receiptRepository).should(org.mockito.Mockito.never())
                        .findByStudentClubAndDateBetween(any(), any(Date.class), any(Date.class));
            }
        }
    }
}
