package com.example.tomyongji.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetConfirmDto {

    @Schema(description = "이메일로 발송된 비밀번호 재설정 토큰 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "토큰은 필수 입력값입니다.")
    private String token;

    @Schema(description = "새로 설정할 비밀번호", example = "newPassword123!")
    @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
    private String newPassword;
}
