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
public class PasswordResetRequestDto {

    @Schema(description = "비밀번호 재설정 링크를 받을 이메일 주소", example = "user@mju.ac.kr")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;
}
