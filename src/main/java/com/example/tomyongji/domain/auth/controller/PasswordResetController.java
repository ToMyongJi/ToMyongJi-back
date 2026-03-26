package com.example.tomyongji.domain.auth.controller;

import com.example.tomyongji.domain.auth.dto.PasswordResetConfirmDto;
import com.example.tomyongji.domain.auth.dto.PasswordResetRequestDto;
import com.example.tomyongji.domain.auth.service.PasswordResetService;
import com.example.tomyongji.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "비밀번호 재설정 api", description = "비밀번호 재설정 링크 발송 및 새 비밀번호 설정과 관련된 API들입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 재설정 메일 발송 api", description = "입력한 이메일로 비밀번호 재설정 링크를 발송합니다. 이메일 존재 여부와 무관하게 항상 성공 응답을 반환합니다.")
    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestReset(@Valid @RequestBody PasswordResetRequestDto dto) {
        passwordResetService.requestPasswordReset(dto.getEmail());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "비밀번호 재설정 확인 api", description = "이메일로 받은 토큰과 새 비밀번호를 입력하여 비밀번호를 변경합니다.")
    @PostMapping("/reset-confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReset(@Valid @RequestBody PasswordResetConfirmDto dto) {
        passwordResetService.confirmPasswordReset(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
