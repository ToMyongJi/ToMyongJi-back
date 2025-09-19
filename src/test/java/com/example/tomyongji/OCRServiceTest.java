package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.receipt.service.OCRService;
import com.example.tomyongji.receipt.service.ReceiptService;
import com.example.tomyongji.validation.CustomException;

import java.lang.reflect.Field;
import java.util.Date;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class OCRServiceTest {

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
        studentClub = StudentClub.builder()
                .id(30L)
                .studentClubName("스마트시스템공과대학 학생회")
                .build();

        testUser = User.builder()
                .id(1L)
                .userId("testUser")
                .name("정우주")
                .studentNum("60221317")
                .studentClub(studentClub)
                .collegeName("스마트시스템공과대학")
                .email("testuser@gmail.com")
                .password("1234")
                .role("PRESIDENT")
                .build();

        currentUser = org.springframework.security.core.userdetails.User.builder()
                .username("testUser")
                .password("1234")
                .authorities("ROLE_PRESIDENT")
                .build();

        ReflectionTestUtils.setField(ocrService, "apiURL", "https://test.api.url");
        ReflectionTestUtils.setField(ocrService, "secretKey", "testSecretKey");

        injectRestTemplateMock();
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

    @Test
    @DisplayName("OCR 처리 성공 테스트")
    void processImage_Success() {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test".getBytes()
        );

        String mockOCRResponse = """
                {
                    "images": [{
                        "receipt": {
                            "result": {
                                "paymentInfo": {
                                    "date": {
                                        "formatted": {
                                            "year": "2020",
                                            "month": "06",
                                            "day": "16"
                                        }
                                    }
                                },
                                "storeInfo": {
                                    "name": {
                                        "text": "다이소"
                                    }
                                },
                                "totalPrice": {
                                    "price": {
                                        "formatted": {
                                            "value": "3,500"
                                        }
                                    }
                                }
                            }
                        }
                    }]
                }
                """;

        ocrService = spy(new OCRService(receiptService, receiptMapper, userRepository));
        ReflectionTestUtils.setField(ocrService, "apiURL", "https://test.api.url");
        ReflectionTestUtils.setField(ocrService, "secretKey", "testSecretKey");

        OCRResultDto expectedResult = new OCRResultDto(new Date(), "다이소", 3500);
        doReturn(expectedResult).when(ocrService).processImage(any(MockMultipartFile.class));

        // When
        OCRResultDto result = ocrService.processImage(imageFile);

        // Then
        assertNotNull(result);
        assertThat(result.getContent()).isEqualTo("다이소");
        assertThat(result.getWithdrawal()).isEqualTo(3500);
    }

    @Test
    @DisplayName("OCR 영수증 업로드 성공 테스트")
    void uploadOcrReceipt_Success() {
        // Given
        String userId = testUser.getUserId();
        OCRResultDto ocrResultDto = new OCRResultDto(
                new Date(), "테스트 상점", 5000
        );

        ReceiptDto receiptDto = ReceiptDto.builder()
                .content("테스트 상점")
                .withdrawal(5000)
                .build();

        ReceiptCreateDto receiptCreateDto = ReceiptCreateDto.builder()
                .userId(userId)
                .content("테스트 상점")
                .withdrawal(5000)
                .build();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(testUser));
        when(receiptMapper.toReceiptDto(ocrResultDto)).thenReturn(receiptDto);
        when(receiptMapper.toReceiptCreateDto(receiptDto)).thenReturn(receiptCreateDto);

        // When
        ocrService.uploadOcrReceipt(ocrResultDto, userId, currentUser);

        // Then
        verify(userRepository).findByUserId(userId);
        verify(receiptMapper).toReceiptDto(ocrResultDto);
        verify(receiptMapper).toReceiptCreateDto(receiptDto);
        verify(receiptService).createReceipt(any(ReceiptCreateDto.class), eq(currentUser));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 OCR 영수증 업로드 실패")
    void uploadOcrReceipt_NotFoundUser() {
        // Given
        String invalidUserId = "nonExistentUser";
        OCRResultDto ocrResultDto = new OCRResultDto(
                new Date(), "테스트 상점", 5000
        );

        when(userRepository.findByUserId(invalidUserId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> ocrService.uploadOcrReceipt(ocrResultDto, invalidUserId, currentUser));

        assertThat(exception.getErrorCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_USER);
        verify(userRepository).findByUserId(invalidUserId);
        verify(receiptService, never()).createReceipt(any(), any());
    }

    @Test
    @DisplayName("JPEG 파일 확장자 변환 테스트")
    void processImage_JpegFormat() {
        // Given
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file", "test.jpeg", "image/jpeg", "test".getBytes()
        );

        // OCRService Mock 설정
        ocrService = spy(new OCRService(receiptService, receiptMapper, userRepository));
        ReflectionTestUtils.setField(ocrService, "apiURL", "https://test.api.url");
        ReflectionTestUtils.setField(ocrService, "secretKey", "testSecretKey");

        OCRResultDto expectedResult = new OCRResultDto(new Date(), "테스트", 1000);
        doReturn(expectedResult).when(ocrService).processImage(any(MockMultipartFile.class));

        // When
        OCRResultDto result = ocrService.processImage(jpegFile);

        // Then
        assertNotNull(result);
        assertThat(result.getWithdrawal()).isEqualTo(1000);
    }

    @Test
    @DisplayName("PDF 파일 형식 지원 테스트")
    void processImage_PdfFormat() {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "pdf".getBytes()
        );

        ocrService = spy(new OCRService(receiptService, receiptMapper, userRepository));
        ReflectionTestUtils.setField(ocrService, "apiURL", "https://test.api.url");
        ReflectionTestUtils.setField(ocrService, "secretKey", "testSecretKey");

        OCRResultDto expectedResult = new OCRResultDto(new Date(), "PDF 상점", 15000);
        doReturn(expectedResult).when(ocrService).processImage(any(MockMultipartFile.class));

        // When
        OCRResultDto result = ocrService.processImage(pdfFile);

        // Then
        assertNotNull(result);
        assertThat(result.getContent()).isEqualTo("PDF 상점");
        assertThat(result.getWithdrawal()).isEqualTo(15000);
    }
}