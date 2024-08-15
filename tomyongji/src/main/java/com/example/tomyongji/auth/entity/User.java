package com.example.tomyongji.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message="사용자 아이디는 필수 입력값입니다")
    private String userId;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;
    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="학부는 필수 입력값입니다")
    private String college;
    @NotBlank(message="전공은 필수 입력값입니다")
    private String major;
    @NotBlank(message="이메일은 필수 입력값입니다")
    private String email;
    @NotBlank(message="비밀번호는 필수 입력값입니다")
    private String password;
    @NotBlank(message="계정 타입은 필수 입력값입니다")
    private String role;
}
