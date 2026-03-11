package com.example.tomyongji.domain.auth.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.auth.dto.EmailDto;
import com.example.tomyongji.domain.auth.dto.VerifyDto;
import com.example.tomyongji.domain.auth.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="이메일 인증 api", description = "이메일 인증번호 발송 및 인증코드 확인과 관련된 API들입니다.")
@RestController
@RequiredArgsConstructor

@RequestMapping("api/users")
public class EmailController {
    private final EmailService emailService;

    @Operation(summary = "이메일 전송 api", description = "인증번호를 이메일로 발송합니다.")
    @PostMapping("/emailCheck")
    public ResponseEntity<ApiResponse<Void>> emailCheck(@RequestBody EmailDto emailDTO) throws MessagingException {
        emailService.sendSimpleMessage(emailDTO.getEmail());

        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
    
    @Operation(summary = "이메일 인증코드 확인 api", description = "사용자가 적은 인증코드를 비교합니다")
    @ResponseBody
    @PostMapping("/verifyCode")
    public ResponseEntity<ApiResponse<Boolean>> verifyCode(@RequestBody VerifyDto verifyDto) {
        boolean isVerified = emailService.verifyCode(verifyDto);
        if (isVerified) {
            return ResponseEntity.ok(ApiResponse.onSuccess(isVerified));}
        else {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.onFailure(401, "인증 코드가 일치하지 않습니다."));
        }
    }
}
