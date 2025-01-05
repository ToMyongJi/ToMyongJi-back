package com.example.tomyongji.auth.service;

import com.example.tomyongji.auth.dto.VerifyDto;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.validation.CustomException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import static com.example.tomyongji.validation.ErrorMsg.ERROR_SEND_EMAIL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private static final String senderEmail = "eeeseohyun@gmail.com";

    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(3);

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26) + 97));
                case 1 -> key.append((char) (random.nextInt(26) + 65));
                case 2 -> key.append(random.nextInt(10));
            }
        }
        return key.toString();
    }

    public MimeMessage createMail(String mail, String number) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("이메일 인증");
        String body = "";
        body += "<h3>요청하신 인증 번호입니다.</h3>";
        body += "<h1>" + number + "</h1>";
        body += "<h3>감사합니다.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    public String sendSimpleMessage(String sendEmail) throws MessagingException {
        String number = createNumber();
        MimeMessage message = createMail(sendEmail, number);
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
            throw new CustomException(ERROR_SEND_EMAIL,400);
        }
        // 인증 코드와 이메일 주소를 저장
        EmailVerification verification = new EmailVerification();
        verification.setEmail(sendEmail);
        verification.setVerificationCode(number);
        verification.setVerificatedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        return number;
    }

    public boolean verifyCode(VerifyDto verifyDto) {
        return emailVerificationRepository.findByEmail(verifyDto.getEmail())
                .map(verification -> verification.getVerificationCode().equals(verifyDto.getCode()))
                .orElse(false);
    }
}
