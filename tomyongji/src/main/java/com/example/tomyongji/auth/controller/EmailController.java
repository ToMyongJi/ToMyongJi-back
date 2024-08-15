package com.example.tomyongji.auth.controller;

import com.example.tomyongji.auth.dto.EmailDto;
import com.example.tomyongji.auth.dto.VerifyDto;
import com.example.tomyongji.auth.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="이메일 인증 api", description = "모든 음식 리스트 조회 기능, 음식 기록 기능")
@RestController
@RequiredArgsConstructor

@RequestMapping("api/users")
public class EmailController {
    private final EmailService emailService;
    
    @Operation(summary = "이메일 전송 api", description = "사용자가 이메일을 입력하고 이메일 인증 버튼을 눌렀을때 인증번호를 전송합니다.")
    @ResponseBody
    @PostMapping("/emailCheck")
    public String emailCheck(@RequestBody EmailDto emailDTO) throws MessagingException {
        String authCode = emailService.sendSimpleMessage(emailDTO.getEmail());
        return authCode;
    }
    
    @Operation(summary = "이메일 인증코드 확인 api", description = "사용자가 적은 인증코드를 비교합니다")
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
