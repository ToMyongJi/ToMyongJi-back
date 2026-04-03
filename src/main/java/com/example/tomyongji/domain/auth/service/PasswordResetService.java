package com.example.tomyongji.domain.auth.service;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.global.error.CustomException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.example.tomyongji.global.error.ErrorMsg.ERROR_SEND_EMAIL;
import static com.example.tomyongji.global.error.ErrorMsg.INVALID_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String REDIS_KEY_PREFIX = "pw_reset:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // 보안상 이메일 존재 여부를 노출하지 않음 — 항상 성공 응답
            return;
        }

        String token = UUID.randomUUID().toString();
        String redisKey = REDIS_KEY_PREFIX + token;
        stringRedisTemplate.opsForValue().set(redisKey, email, TOKEN_TTL);

        try {
            MimeMessage message = createResetMail(email, token);
            javaMailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", e.getMessage());
            stringRedisTemplate.delete(redisKey);
            throw new CustomException(ERROR_SEND_EMAIL, 422);
        }
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String redisKey = REDIS_KEY_PREFIX + token;
        String email = stringRedisTemplate.opsForValue().get(redisKey);

        if (email == null) {
            throw new CustomException(INVALID_TOKEN, 400);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(INVALID_TOKEN, 400));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        stringRedisTemplate.delete(redisKey);
    }

    private MimeMessage createResetMail(String to, String token) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject("비밀번호 재설정 요청");

        String resetLink = "https://tomyongji.com/password/reset?token=" + token;
        String body = "<h3>비밀번호 재설정 요청</h3>"
                + "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>"
                + "<a href=\"" + resetLink + "\">비밀번호 재설정</a>"
                + "<p>이 링크는 15분 후 만료됩니다.</p>";
        message.setText(body, "UTF-8", "html");

        return message;
    }


    // 프론트 측 테스트를 위한 메서드 (개발용 URL 주소 으로 변경)

    public void requestPasswordResetTest(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // 보안상 이메일 존재 여부를 노출하지 않음 — 항상 성공 응답
            return;
        }

        String token = UUID.randomUUID().toString();
        String redisKey = REDIS_KEY_PREFIX + token;
        stringRedisTemplate.opsForValue().set(redisKey, email, TOKEN_TTL);

        try {
            MimeMessage message = createResetMailTest(email, token);
            javaMailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("비밀번호 재설정 메일 발송 실패: {}", e.getMessage());
            stringRedisTemplate.delete(redisKey);
            throw new CustomException(ERROR_SEND_EMAIL, 422);
        }
    }

    private MimeMessage createResetMailTest(String to, String token) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject("비밀번호 재설정 요청");

        String resetLink = "https://new.tomyongji-dev.shop/password/reset?token=" + token;
        String body = "<h3>비밀번호 재설정 요청</h3>"
                + "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>"
                + "<a href=\"" + resetLink + "\">비밀번호 재설정</a>"
                + "<p>이 링크는 15분 후 만료됩니다.</p>";
        message.setText(body, "UTF-8", "html");

        return message;
    }
}
