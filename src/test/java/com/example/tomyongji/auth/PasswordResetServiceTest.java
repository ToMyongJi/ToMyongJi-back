package com.example.tomyongji.auth;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.auth.service.PasswordResetService;
import com.example.tomyongji.global.error.CustomException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.example.tomyongji.global.error.ErrorMsg.ERROR_SEND_EMAIL;
import static com.example.tomyongji.global.error.ErrorMsg.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService 클래스")
class PasswordResetServiceTest {

    @InjectMocks
    PasswordResetService passwordResetService;

    @Mock
    StringRedisTemplate stringRedisTemplate;
    @Mock
    JavaMailSender javaMailSender;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ValueOperations<String, String> valueOperations;
    @Mock
    MimeMessage mimeMessage;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .userId("tomyongji2024")
                .name("투명지")
                .password("encoded_password")
                .role("STU")
                .email("user@mju.ac.kr")
                .collegeName("ICT융합대학")
                .studentNum("60222024")
                .build();
    }

    @Nested
    @DisplayName("requestPasswordReset 메서드는")
    class Describe_requestPasswordReset {

        @Nested
        @DisplayName("등록된 이메일이 주어지면")
        class Context_with_registered_email {

            @BeforeEach
            void setUp() throws MessagingException {
                given(userRepository.findByEmail("user@mju.ac.kr")).willReturn(Optional.of(user));
                given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
                given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
            }

            @Test
            @DisplayName("Redis에 토큰을 저장하고 메일을 발송한다")
            void it_saves_token_and_sends_mail() throws MessagingException {
                // when
                passwordResetService.requestPasswordReset("user@mju.ac.kr");

                // then
                then(valueOperations).should().set(
                        argThat(key -> key.startsWith("pw_reset:")),
                        eq("user@mju.ac.kr"),
                        any()
                );
                then(javaMailSender).should().send(mimeMessage);
            }
        }

        @Nested
        @DisplayName("등록되지 않은 이메일이 주어지면")
        class Context_with_unregistered_email {

            @BeforeEach
            void setUp() {
                given(userRepository.findByEmail("unknown@mju.ac.kr")).willReturn(Optional.empty());
            }

            @Test
            @DisplayName("아무 동작 없이 정상 반환한다 (보안상 이메일 존재 여부 미노출)")
            void it_returns_silently() {
                // when & then (예외 없이 정상 종료)
                passwordResetService.requestPasswordReset("unknown@mju.ac.kr");

                then(stringRedisTemplate).should(never()).opsForValue();
                then(javaMailSender).should(never()).send(any(MimeMessage.class));
            }
        }

        @Nested
        @DisplayName("메일 발송에 실패하면")
        class Context_when_mail_send_fails {

            @BeforeEach
            void setUp() {
                given(userRepository.findByEmail("user@mju.ac.kr")).willReturn(Optional.of(user));
                given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
                given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
                willThrow(new org.springframework.mail.MailSendException("SMTP error"))
                        .given(javaMailSender).send(any(MimeMessage.class));
            }

            @Test
            @DisplayName("Redis에 저장된 토큰을 삭제하고 ERROR_SEND_EMAIL 예외를 던진다")
            void it_deletes_token_and_throws_exception() {
                // when & then
                assertThatThrownBy(() -> passwordResetService.requestPasswordReset("user@mju.ac.kr"))
                        .isInstanceOf(CustomException.class)
                        .satisfies(ex -> {
                            CustomException customEx = (CustomException) ex;
                            assertThat(customEx.getMessage()).isEqualTo(ERROR_SEND_EMAIL);
                            assertThat(customEx.getErrorCode()).isEqualTo(422);
                        });

                then(stringRedisTemplate).should().delete(argThat((String key) -> key.startsWith("pw_reset:")));
            }
        }
    }

    @Nested
    @DisplayName("confirmPasswordReset 메서드는")
    class Describe_confirmPasswordReset {

        @Nested
        @DisplayName("유효한 토큰과 새 비밀번호가 주어지면")
        class Context_with_valid_token {

            @BeforeEach
            void setUp() {
                given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
                given(valueOperations.get("pw_reset:valid-token")).willReturn("user@mju.ac.kr");
                given(userRepository.findByEmail("user@mju.ac.kr")).willReturn(Optional.of(user));
                given(passwordEncoder.encode("newPassword123!")).willReturn("new_encoded_password");
            }

            @Test
            @DisplayName("비밀번호를 변경하고 토큰을 즉시 무효화한다")
            void it_changes_password_and_invalidates_token() {
                // when
                passwordResetService.confirmPasswordReset("valid-token", "newPassword123!");

                // then
                then(userRepository).should().save(argThat(savedUser ->
                        savedUser.getPassword().equals("new_encoded_password")
                ));
                then(stringRedisTemplate).should().delete("pw_reset:valid-token");
            }
        }

        @Nested
        @DisplayName("만료되거나 존재하지 않는 토큰이 주어지면")
        class Context_with_invalid_token {

            @BeforeEach
            void setUp() {
                given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
                given(valueOperations.get("pw_reset:expired-token")).willReturn(null);
            }

            @Test
            @DisplayName("INVALID_TOKEN 예외를 던진다")
            void it_throws_invalid_token_exception() {
                // when & then
                assertThatThrownBy(() -> passwordResetService.confirmPasswordReset("expired-token", "newPassword123!"))
                        .isInstanceOf(CustomException.class)
                        .satisfies(ex -> {
                            CustomException customEx = (CustomException) ex;
                            assertThat(customEx.getMessage()).isEqualTo(INVALID_TOKEN);
                            assertThat(customEx.getErrorCode()).isEqualTo(400);
                        });

                then(userRepository).should(never()).findByEmail(any());
            }
        }
    }
}
