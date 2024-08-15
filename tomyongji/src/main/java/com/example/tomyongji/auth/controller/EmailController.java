package com.example.tomyongji.auth.controller;

import com.example.tomyongji.auth.dto.EmailDto;
import com.example.tomyongji.auth.dto.VerifyDto;
import com.example.tomyongji.auth.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class EmailController {
    private final EmailService emailService;

    @ResponseBody
    @PostMapping("/emailCheck")
    public String emailCheck(@RequestBody EmailDto emailDTO) throws MessagingException {
        String authCode = emailService.sendSimpleMessage(emailDTO.getEmail());
        return authCode;
    }

    @ResponseBody
    @PostMapping("/verifyCode")
    public String verifyCode(@RequestBody VerifyDto verifyDto) {
        boolean isVerified = emailService.verifyCode(verifyDto);
        if (isVerified) {
            return "인증 성공";
        } else {
            return "인증 실패";
        }
    }
}
