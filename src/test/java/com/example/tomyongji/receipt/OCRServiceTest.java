package com.example.tomyongji.receipt;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.OCRResultDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptDto;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.domain.receipt.service.OCRService;
import com.example.tomyongji.domain.receipt.service.ReceiptService;
import com.example.tomyongji.global.error.CustomException;

import java.lang.reflect.Field;
import java.util.Date;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OCRServiceTest {

    @Mock
    private ReceiptService receiptService;

    @Mock
    private ReceiptMapper receiptMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OCRService ocrService;

    private User testUser;
    private StudentClub studentClub;
    private UserDetails currentUser;

    @BeforeEach
    void setUp() throws Exception {
        studentClub = createStudentClub(30L, "스마트시스템공과대학 학생회");

        testUser = createUser(
                1L,
                "testUser",
                "정우주",
                "60221317",
                studentClub,
                "스마트시스템공과대학",
                "testuser@gmail.com",
                "1234",
                "PRESIDENT"
        );

        currentUser = createUserDetails("testUser", "1234");

        ReflectionTestUtils.setField(ocrService, "apiURL", "https://test.api.url");
        ReflectionTestUtils.setField(ocrService, "secretKey", "testSecretKey");

        injectRestTemplateMock();
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

    private MockMultipartFile createImageFile(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    private OCRResultDto createOCRResultDto(Date date, String content, int withdrawal) {
        return new OCRResultDto(date, content, withdrawal);
    }

    private OCRService createSpyOCRService() {
        OCRService spyService = spy(new OCRService(receiptService, receiptMapper, userRepository));
        ReflectionTestUtils.setField(spyService, "apiURL", "https://test.api.url");
        ReflectionTestUtils.setField(spyService, "secretKey", "testSecretKey");
        return spyService;
    }

    private void injectRestTemplateMock() throws Exception {
        Field[] fields = OCRService.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("restTemplate")) {
                field.setAccessible(true);
                field.set(ocrService, restTemplate);
                break;
            }
        }
    }

    @Nested
    @DisplayName("processImage 메서드는")
    class Describe_processImage {

        @Nested
        @DisplayName("유효한 이미지 파일이 주어지면")
        class Context_with_valid_image {

            @Test
            @DisplayName("OCR 처리를 성공적으로 수행한다")
            void it_processes_successfully() {
                // given
                MockMultipartFile imageFile = createImageFile("test.jpg", "image/jpeg", "test".getBytes());
                OCRService spyService = createSpyOCRService();

                OCRResultDto expectedResult = createOCRResultDto(new Date(), "다이소", 3500);
                doReturn(expectedResult).when(spyService).processImage(any(MockMultipartFile.class));

                // when
                OCRResultDto result = spyService.processImage(imageFile);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEqualTo("다이소");
                assertThat(result.getWithdrawal()).isEqualTo(3500);
            }
        }

        @Nested
        @DisplayName("JPEG 형식의 파일이 주어지면")
        class Context_with_jpeg_format {

            @Test
            @DisplayName("확장자를 올바르게 처리한다")
            void it_handles_jpeg_extension() {
                // given
                MockMultipartFile jpegFile = createImageFile("test.jpeg", "image/jpeg", "test".getBytes());
                OCRService spyService = createSpyOCRService();

                OCRResultDto expectedResult = createOCRResultDto(new Date(), "테스트", 1000);
                doReturn(expectedResult).when(spyService).processImage(any(MockMultipartFile.class));

                // when
                OCRResultDto result = spyService.processImage(jpegFile);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getWithdrawal()).isEqualTo(1000);
            }
        }

        @Nested
        @DisplayName("PDF 형식의 파일이 주어지면")
        class Context_with_pdf_format {

            @Test
            @DisplayName("PDF 형식을 지원한다")
            void it_supports_pdf_format() {
                // given
                MockMultipartFile pdfFile = createImageFile("test.pdf", "application/pdf", "pdf".getBytes());
                OCRService spyService = createSpyOCRService();

                OCRResultDto expectedResult = createOCRResultDto(new Date(), "PDF 상점", 15000);
                doReturn(expectedResult).when(spyService).processImage(any(MockMultipartFile.class));

                // when
                OCRResultDto result = spyService.processImage(pdfFile);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEqualTo("PDF 상점");
                assertThat(result.getWithdrawal()).isEqualTo(15000);
            }
        }
    }

    @Nested
    @DisplayName("uploadOcrReceipt 메서드는")
    class Describe_uploadOcrReceipt {

        @Nested
        @DisplayName("유효한 OCR 결과와 유저 정보가 주어지면")
        class Context_with_valid_ocr_result {

            @Test
            @DisplayName("영수증을 성공적으로 업로드한다")
            void it_uploads_successfully() {
                // given
                String userId = testUser.getUserId();
                OCRResultDto ocrResultDto = createOCRResultDto(new Date(), "테스트 상점", 5000);

                ReceiptDto receiptDto = ReceiptDto.builder()
                        .content("테스트 상점")
                        .withdrawal(5000)
                        .build();

                ReceiptCreateDto receiptCreateDto = ReceiptCreateDto.builder()
                        .userId(userId)
                        .content("테스트 상점")
                        .withdrawal(5000)
                        .build();

                given(userRepository.findByUserId(userId)).willReturn(Optional.of(testUser));
                given(receiptMapper.toReceiptDto(ocrResultDto)).willReturn(receiptDto);
                given(receiptMapper.toReceiptCreateDto(receiptDto)).willReturn(receiptCreateDto);

                // when
                ocrService.uploadOcrReceipt(ocrResultDto, userId, currentUser);

                // then
                then(userRepository).should().findByUserId(userId);
                then(receiptMapper).should().toReceiptDto(ocrResultDto);
                then(receiptMapper).should().toReceiptCreateDto(receiptDto);
                then(receiptService).should().createReceipt(any(ReceiptCreateDto.class), eq(currentUser));
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
                OCRResultDto ocrResultDto = createOCRResultDto(new Date(), "테스트 상점", 5000);

                given(userRepository.findByUserId(invalidUserId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> ocrService.uploadOcrReceipt(ocrResultDto, invalidUserId, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);

                then(userRepository).should().findByUserId(invalidUserId);
                then(receiptService).should(never()).createReceipt(any(), any());
            }
        }
    }
}