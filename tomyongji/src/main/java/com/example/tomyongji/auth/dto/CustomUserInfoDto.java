package com.example.tomyongji.auth.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomUserInfoDto {
    @Id
    private Long id;
    @NotBlank(message="사용자 아이디는 필수 입력값입니다")
    private String userId;
    @NotBlank(message="비밀번호는 필수 입력값입니다")
    private String password;
    @NotBlank(message="계정 타입은 필수 입력값입니다")
    private String role;
}
