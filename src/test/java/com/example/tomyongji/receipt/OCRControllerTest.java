package com.example.tomyongji.receipt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.tomyongji.config.SecurityConfig;
import com.example.tomyongji.domain.auth.jwt.JwtProvider;
import com.example.tomyongji.domain.receipt.controller.OCRController;
import com.example.tomyongji.domain.receipt.dto.OCRResultDto;
import com.example.tomyongji.domain.receipt.service.OCRService;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OCRController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class OCRControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OCRService ocrService;

    @MockBean
    private JwtProvider jwtProvider;

    private Logger controllerLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        // OCRController 클래스의 로거를 캡처
        controllerLogger = (Logger) LoggerFactory.getLogger(OCRController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerLogger.addAppender(listAppender);
        controllerLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void tearDown() {
        controllerLogger.detachAppender(listAppender);
    }

    @Nested
    @DisplayName("uploadImageAndExtractText 메서드는")
    class Describe_uploadImageAndExtractText {

        @Nested
        @DisplayName("인증된 사용자가 이미지 파일을 업로드하면")
        class Context_with_authenticated_user_and_valid_file {

            @Test
            @WithMockUser(username = "testUser", roles = {"PRESIDENT"})
            @DisplayName("진입 로그에 [OCR_USAGE] 태그가 포함된다")
            void uploadImageAndExtractText_logsOcrUsageTag() throws Exception {
                // given
                String userId = "testUser";
                String filename = "receipt.jpg";
                MockMultipartFile file = new MockMultipartFile(
                        "file", filename, "image/jpeg", "test-image-content".getBytes()
                );
                OCRResultDto resultDto = new OCRResultDto(new Date(), "테스트 상점", 5000);

                given(ocrService.processImage(any())).willReturn(resultDto);
                willDoNothing().given(ocrService).uploadOcrReceipt(any(), eq(userId), any(UserDetails.class));

                // when
                mockMvc.perform(multipart("/api/ocr/upload/{userId}", userId)
                                .file(file))
                        .andExpect(status().isCreated());

                // then
                boolean hasOcrUsageTag = listAppender.list.stream()
                        .filter(event -> event.getLevel() == Level.INFO)
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("[OCR_USAGE]"));

                assertThat(hasOcrUsageTag)
                        .as("uploadImageAndExtractText 진입 시 INFO 레벨로 [OCR_USAGE] 태그가 포함된 로그가 기록되어야 한다")
                        .isTrue();
            }

            @Test
            @WithMockUser(username = "testUser", roles = {"PRESIDENT"})
            @DisplayName("진입 로그에 event=request 가 포함된다")
            void uploadImageAndExtractText_logsEventRequest() throws Exception {
                // given
                String userId = "testUser";
                String filename = "receipt.jpg";
                MockMultipartFile file = new MockMultipartFile(
                        "file", filename, "image/jpeg", "test-image-content".getBytes()
                );
                OCRResultDto resultDto = new OCRResultDto(new Date(), "테스트 상점", 5000);

                given(ocrService.processImage(any())).willReturn(resultDto);
                willDoNothing().given(ocrService).uploadOcrReceipt(any(), eq(userId), any(UserDetails.class));

                // when
                mockMvc.perform(multipart("/api/ocr/upload/{userId}", userId)
                                .file(file))
                        .andExpect(status().isCreated());

                // then
                boolean hasEventRequest = listAppender.list.stream()
                        .filter(event -> event.getLevel() == Level.INFO)
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("[OCR_USAGE]") && msg.contains("event=request"));

                assertThat(hasEventRequest)
                        .as("uploadImageAndExtractText 진입 로그에 event=request 가 포함되어야 한다")
                        .isTrue();
            }

            @Test
            @WithMockUser(username = "testUser", roles = {"PRESIDENT"})
            @DisplayName("진입 로그에 userId 값이 포함된다")
            void uploadImageAndExtractText_logsUserId() throws Exception {
                // given
                String userId = "testUser";
                String filename = "receipt.jpg";
                MockMultipartFile file = new MockMultipartFile(
                        "file", filename, "image/jpeg", "test-image-content".getBytes()
                );
                OCRResultDto resultDto = new OCRResultDto(new Date(), "테스트 상점", 5000);

                given(ocrService.processImage(any())).willReturn(resultDto);
                willDoNothing().given(ocrService).uploadOcrReceipt(any(), eq(userId), any(UserDetails.class));

                // when
                mockMvc.perform(multipart("/api/ocr/upload/{userId}", userId)
                                .file(file))
                        .andExpect(status().isCreated());

                // then
                boolean hasUserId = listAppender.list.stream()
                        .filter(event -> event.getLevel() == Level.INFO)
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("[OCR_USAGE]") && msg.contains(userId));

                assertThat(hasUserId)
                        .as("uploadImageAndExtractText 진입 로그에 userId=" + userId + " 값이 포함되어야 한다")
                        .isTrue();
            }

            @Test
            @WithMockUser(username = "testUser", roles = {"PRESIDENT"})
            @DisplayName("진입 로그에 filename 값이 포함된다")
            void uploadImageAndExtractText_logsFilename() throws Exception {
                // given
                String userId = "testUser";
                String filename = "receipt.jpg";
                MockMultipartFile file = new MockMultipartFile(
                        "file", filename, "image/jpeg", "test-image-content".getBytes()
                );
                OCRResultDto resultDto = new OCRResultDto(new Date(), "테스트 상점", 5000);

                given(ocrService.processImage(any())).willReturn(resultDto);
                willDoNothing().given(ocrService).uploadOcrReceipt(any(), eq(userId), any(UserDetails.class));

                // when
                mockMvc.perform(multipart("/api/ocr/upload/{userId}", userId)
                                .file(file))
                        .andExpect(status().isCreated());

                // then
                boolean hasFilename = listAppender.list.stream()
                        .filter(event -> event.getLevel() == Level.INFO)
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("[OCR_USAGE]") && msg.contains(filename));

                assertThat(hasFilename)
                        .as("uploadImageAndExtractText 진입 로그에 filename=" + filename + " 값이 포함되어야 한다")
                        .isTrue();
            }

            @Test
            @WithMockUser(username = "testUser", roles = {"PRESIDENT"})
            @DisplayName("진입 로그는 processImage 호출 전에 기록된다 (로그가 존재해야 함)")
            void uploadImageAndExtractText_logsBeforeProcessImage() throws Exception {
                // given
                String userId = "testUser";
                String filename = "receipt.jpg";
                MockMultipartFile file = new MockMultipartFile(
                        "file", filename, "image/jpeg", "test-image-content".getBytes()
                );
                OCRResultDto resultDto = new OCRResultDto(new Date(), "테스트 상점", 5000);

                given(ocrService.processImage(any())).willReturn(resultDto);
                willDoNothing().given(ocrService).uploadOcrReceipt(any(), eq(userId), any(UserDetails.class));

                // when
                mockMvc.perform(multipart("/api/ocr/upload/{userId}", userId)
                                .file(file))
                        .andExpect(status().isCreated());

                // then: 진입 로그에 [OCR_USAGE], userId, filename, event=request 가 모두 포함
                boolean hasFullRequestLog = listAppender.list.stream()
                        .filter(event -> event.getLevel() == Level.INFO)
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("[OCR_USAGE]")
                                && msg.contains("userId=" + userId)
                                && msg.contains("filename=" + filename)
                                && msg.contains("event=request"));

                assertThat(hasFullRequestLog)
                        .as("processImage 호출 전 [OCR_USAGE] userId=%s filename=%s event=request 형식의 INFO 로그가 기록되어야 한다"
                                .formatted(userId, filename))
                        .isTrue();
            }
        }

        @Nested
        @DisplayName("인증되지 않은 사용자가 요청하면")
        class Context_with_unauthenticated_user {

            @Test
            @DisplayName("403 Forbidden 을 반환하고 로그가 기록되지 않는다")
            void uploadImageAndExtractText_returns403WhenUnauthenticated() throws Exception {
                // given
                String userId = "testUser";
                MockMultipartFile file = new MockMultipartFile(
                        "file", "receipt.jpg", "image/jpeg", "test-image-content".getBytes()
                );

                // when
                mockMvc.perform(multipart("/api/ocr/upload/{userId}", userId)
                                .file(file))
                        .andExpect(status().isForbidden());

                // then: 인증 실패이므로 컨트롤러 진입 자체가 없어 [OCR_USAGE] 로그가 없어야 한다
                boolean hasOcrUsageLog = listAppender.list.stream()
                        .map(ILoggingEvent::getFormattedMessage)
                        .anyMatch(msg -> msg.contains("[OCR_USAGE]"));

                assertThat(hasOcrUsageLog)
                        .as("인증되지 않은 요청에서는 컨트롤러에 진입하지 않으므로 [OCR_USAGE] 로그가 없어야 한다")
                        .isFalse();
            }
        }
    }
}
