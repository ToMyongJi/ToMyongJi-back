package com.example.tomyongji.auth.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequsetDto {

    @NotBlank(message="사용자 아이디는 필수 입력값입니다")
    private String userId;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;
    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="학부는 필수 입력값입니다")
    private String college;
    @NotNull(message="학생회Id는 필수 입력값입니다")
    private long studentClubId;
    @NotBlank(message="이메일은 필수 입력값입니다")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;
    @NotBlank(message="비밀번호는 필수 입력값입니다")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
            message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
    private String password;
    @NotBlank(message="계정 타입은 필수 입력값입니다")
    @Pattern(regexp = "^(STU|ADMIN|PRESIDENT)$", message = "계정 타입은 STU, ADMIN,PRESIDENT 중 하나여야 합니다.")
    private String role;


}
